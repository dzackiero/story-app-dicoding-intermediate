package com.pnj.storyapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pnj.storyapp.R
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.databinding.ActivityHomeBinding
import com.pnj.storyapp.ui.ViewModelFactory
import com.pnj.storyapp.ui.adapter.StoryAdapter
import com.pnj.storyapp.ui.add_story.AddStoryActivity
import com.pnj.storyapp.ui.main.MainActivity
import com.pnj.storyapp.util.Result
import com.pnj.storyapp.util.showToast

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSessionData().observe(this@HomeActivity) { user ->
            if (!user.isLogin) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                setupView()
                loadStories(user)
                setupAction(user)
            }
        }
    }

    private fun setupView() {
        viewModel.getThemeSetting().observe(this) {
            isDarkMode = it
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupAction(user: UserModel) {
        binding.fabNewStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

        val logoutMenu: MenuItem = binding.topAppBar.menu.findItem(R.id.action_logout)
        logoutMenu.setOnMenuItemClickListener {
            MaterialAlertDialogBuilder(this@HomeActivity)
                .setTitle(getString(R.string.are_you_sure_you_want_to_logout))
                .setPositiveButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.logout)) { dialog, _ ->
                    dialog.dismiss()
                    viewModel.logout()
                }
                .create().show()
            true
        }

        val toggleThemeMenu: MenuItem = binding.topAppBar.menu.findItem(R.id.action_toggle_theme)
        toggleThemeMenu.setOnMenuItemClickListener {
            viewModel.saveThemeSetting(!isDarkMode)
            true
        }

        binding.swipeStories.setOnRefreshListener { loadStories(user) }
    }

    private fun loadStories(user: UserModel) {
        viewModel.getStories(user.token).observe(this) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> {
                        binding.swipeStories.isRefreshing = true
                        binding.noInternetIcon.isVisible = false
                    }

                    is Result.Error -> {
                        showToast(result.error)
                        binding.apply {
                            swipeStories.isRefreshing = false
                            noInternetIcon.isVisible = true
                        }
                    }

                    is Result.Success -> {
                        binding.swipeStories.isRefreshing = false
                        binding.noInternetIcon.isVisible = false
                        val layoutManager = LinearLayoutManager(this)
                        binding.rvStories.layoutManager = layoutManager
                        binding.rvStories.adapter = StoryAdapter(result.data.listStory)
                    }
                }
            }
        }
    }
}