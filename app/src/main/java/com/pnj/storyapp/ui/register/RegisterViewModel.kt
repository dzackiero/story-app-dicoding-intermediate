package com.pnj.storyapp.ui.register

import androidx.lifecycle.ViewModel
import com.pnj.storyapp.data.repository.StoryRepository

class RegisterViewModel(private val mStoryRepository: StoryRepository) : ViewModel() {

    fun register(name: String, email: String, password: String) =
        mStoryRepository.registerUser(name, email, password)

    fun login(email: String, password: String) =
        mStoryRepository.loginUser(email, password)
}