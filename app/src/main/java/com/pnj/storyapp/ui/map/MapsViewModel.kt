package com.pnj.storyapp.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.data.repository.StoryRepository

class MapsViewModel(private val mStoryRepository: StoryRepository) : ViewModel() {

    fun getStories(token: String) = mStoryRepository.getStoriesWithLocation(token)
    fun getSessionData(): LiveData<UserModel> = mStoryRepository.getSession().asLiveData()
}