package com.pnj.storyapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pnj.storyapp.R
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.databinding.ActivityHomeBinding
import com.pnj.storyapp.ui.ViewModelFactory
import com.pnj.storyapp.ui.adapter.LoadingStateAdapter
import com.pnj.storyapp.ui.adapter.StoryListAdapter
import com.pnj.storyapp.ui.add_story.AddStoryActivity
import com.pnj.storyapp.ui.main.MainActivity
import com.pnj.storyapp.ui.map.MapsActivity
import com.pnj.storyapp.util.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private var isDarkMode = false
    private val adapter = StoryListAdapter()

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
                setupAction()
                loadStories(user)
            }
        }
    }

    private fun setupView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager

        viewModel.getThemeSetting().observe(this) {
            isDarkMode = it
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupAction() {
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

        val mapMenu: MenuItem = binding.topAppBar.menu.findItem(R.id.action_goto_map)
        mapMenu.setOnMenuItemClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            true
        }

        binding.swipeStories.setOnRefreshListener {
            binding.swipeStories.isRefreshing = true
            adapter.refresh()
        }
    }

    private fun loadStories(user: UserModel) {
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            LoadingStateAdapter {
                adapter.retry()
            }
        )

        viewModel.getStories(user.token).observe(this) {
            adapter.submitData(lifecycle, it)

            binding.noInternetIcon.isVisible = adapter.itemCount < 0
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.NotLoading -> {
                        binding.swipeStories.isRefreshing = false
                    }

                    is LoadState.Error -> {
                        binding.swipeStories.isRefreshing = false
                        showToast(
                            getString(
                                R.string.error,
                                (loadStates.refresh as LoadState.Error).error.message.toString()
                            )
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}