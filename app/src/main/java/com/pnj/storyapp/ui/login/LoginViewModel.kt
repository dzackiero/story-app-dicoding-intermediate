package com.pnj.storyapp.ui.login

import androidx.lifecycle.ViewModel
import com.pnj.storyapp.data.repository.StoryRepository

class LoginViewModel(
    private val mStoryRepository: StoryRepository
) : ViewModel() {

    fun login(email: String, password: String) =
        mStoryRepository.loginUser(email, password)
}