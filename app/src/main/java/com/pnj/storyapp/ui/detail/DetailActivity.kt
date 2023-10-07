package com.pnj.storyapp.ui.detail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pnj.storyapp.databinding.ActivityDetailBinding
import com.pnj.storyapp.ui.ViewModelFactory
import com.pnj.storyapp.util.Result
import com.pnj.storyapp.util.loadImage
import com.pnj.storyapp.util.showToast

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels { ViewModelFactory.getInstance(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra(ID_KEY)

        if (storyId != null) {
            viewModel.getSessionData().observe(this) { user ->
                loadStory(user.token, storyId)
            }
        }
    }

    private fun loadStory(token: String, id: String) {
        viewModel.getDetailStory(token, id).observe(this) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        binding.progressbar.isVisible = true
                    }

                    is Result.Error -> {
                        binding.progressbar.isVisible = false
                        showToast(result.error)
                    }

                    is Result.Success -> {
                        binding.progressbar.isVisible = false
                        val story = result.data.story
                        binding.apply {
                            tvDetailName.text = story.name
                            tvDetailDescription.text = story.description
                            ivDetailPhoto.loadImage(this@DetailActivity, story.photoUrl)
                        }
                    }
                }
            }

        }

    }

    companion object {
        const val ID_KEY = "id_key"
    }
}