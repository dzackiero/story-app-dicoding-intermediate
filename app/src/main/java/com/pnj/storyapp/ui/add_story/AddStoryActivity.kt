package com.pnj.storyapp.ui.add_story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.pnj.storyapp.R
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.databinding.ActivityAddStoryBinding
import com.pnj.storyapp.ui.ViewModelFactory
import com.pnj.storyapp.ui.camera.CameraActivity
import com.pnj.storyapp.ui.camera.CameraActivity.Companion.CAMERAX_RESULT
import com.pnj.storyapp.ui.home.HomeActivity
import com.pnj.storyapp.ui.main.MainActivity
import com.pnj.storyapp.util.Result
import com.pnj.storyapp.util.ValidateType
import com.pnj.storyapp.util.reduceFileImage
import com.pnj.storyapp.util.showLoading
import com.pnj.storyapp.util.showToast
import com.pnj.storyapp.util.uriToFile
import com.pnj.storyapp.util.validate
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: AddStoryViewModel by viewModels { ViewModelFactory.getInstance(this) }

    private var currentImageUri: Uri? = null
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast(getString(R.string.permission_request_granted), true)
            } else {
                showToast(getString(R.string.permission_request_denied), true)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        viewModel.getSessionData().observe(this@AddStoryActivity) { user ->
            if (!user.isLogin) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                setupAction(user)
            }
        }
    }

    private fun setupAction(user: UserModel) {
        binding.bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.open_gallery -> {
                    startGallery()
                    true
                }

                R.id.open_camera -> {
                    startCameraX()
                    true
                }

                else -> false
            }
        }
        binding.ivPreview.setOnClickListener {
            UploadFragment().show(supportFragmentManager, "dialog")
        }

        binding.fabUploadStory.setOnClickListener {
            if (binding.edAddDescription.validate(
                    "Description cannot be empty",
                    ValidateType.REQUIRED
                )
            ) uploadImage(user.token, binding.edAddDescription.text.toString())
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    private fun showImage() {
        currentImageUri?.let { uri ->
            Log.d("Image URI", "showImage: $uri")
            binding.ivPreview.setImageURI(uri)
        } ?: {
            Log.d("Empty Image", "Image is empty")
        }
    }

    private fun uploadImage(token: String, description: String) {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            viewModel.uploadImage(token, multipartBody, requestBody).observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            binding.fabUploadStory.showLoading(true)
                            binding.fabUploadStory.isEnabled = false
                        }

                        is Result.Error -> {
                            binding.fabUploadStory.showLoading(
                                false,
                                ContextCompat.getDrawable(this, R.drawable.ic_send)
                            )
                            binding.fabUploadStory.isEnabled = true
                            showToast(result.error)
                        }

                        is Result.Success -> {
                            binding.fabUploadStory.showLoading(
                                false,
                                ContextCompat.getDrawable(this, R.drawable.ic_send)
                            )
                            binding.fabUploadStory.isEnabled = true
                            showToast(result.data.message)

                            val intent = Intent(this, HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
            }

        } ?: showToast(getString(R.string.image_is_required_to_make_a_story))
    }

    fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", getString(R.string.no_media_selected))
            showToast(getString(R.string.no_media_selected))
        }
    }

    fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}