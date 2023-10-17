package com.pnj.storyapp.ui.detail

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pnj.storyapp.data.model.Story
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

        val story = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(STORY_KEY, Story::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(STORY_KEY)
        }

        if (story != null) {
            viewModel.getSessionData().observe(this) { user ->
                loadStory(user.token, story)
            }

        }
    }

    private fun loadStory(token: String, localStory: Story) {
        viewModel.getDetailStory(token, localStory.id).observe(this) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        binding.progressbar.isVisible = true
                    }

                    is Result.Error -> {
                        binding.progressbar.isVisible = false
                        binding.apply {
                            tvDetailName.text = localStory.name
                            tvDetailDescription.text = localStory.description
                            ivDetailPhoto.loadImage(this@DetailActivity, localStory.photoUrl)
                        }
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
        const val STORY_KEY = "story_key"
    }
}