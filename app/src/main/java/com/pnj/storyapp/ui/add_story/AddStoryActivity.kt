package com.pnj.storyapp.ui.add_story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: AddStoryViewModel by viewModels { ViewModelFactory.getInstance(this) }

    private var currentImageUri: Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                showToast(getString(R.string.permission_request_granted), true)
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                showToast(getString(R.string.permission_request_granted), true)
            }

            else -> {
                showToast(getString(R.string.permission_request_denied), true)
            }
        }
    }

    private fun cameraPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun locationPermissionGranted() =
        (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!cameraPermissionGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (!locationPermissionGranted()) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

    private fun showImage() {
        currentImageUri?.let { uri ->
            binding.ivPreview.setImageURI(uri)
        } ?: showToast("Image is empty")
    }

    private fun uploadImage(token: String, description: String) {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    val isChecked = binding.checkboxAddLocation.isChecked
                    val lat: Double? = if (isChecked) loc.latitude else null
                    val lon: Double? = if (isChecked) loc.longitude else null

                    uploadStory(token, imageFile, description, lat, lon)
                }
            } else {
                uploadStory(token, imageFile, description)
            }
        } ?: showToast(getString(R.string.image_is_required_to_make_a_story))
    }

    private fun uploadStory(
        token: String,
        imageFile: File,
        description: String,
        lat: Double? = null,
        lon: Double? = null
    ) {
        viewModel.uploadImage(token, imageFile, description, lat, lon)
            .observe(this) { result ->
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
}