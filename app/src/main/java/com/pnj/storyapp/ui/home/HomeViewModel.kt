package com.pnj.storyapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val mStoryRepository: StoryRepository) : ViewModel() {
    fun getSessionData(): LiveData<UserModel> =
        mStoryRepository.getSession().asLiveData()

    fun getStories(token: String) =
        mStoryRepository.getStories(token).cachedIn(viewModelScope)

    fun getThemeSetting(): LiveData<Boolean> = mStoryRepository.getThemeSetting().asLiveData()

    fun saveThemeSetting(isDarkMode: Boolean) {
        viewModelScope.launch {
            mStoryRepository.saveThemeSetting(isDarkMode)
        }
    }

    fun logout() {
        viewModelScope.launch {
            mStoryRepository.logout()
        }
    }
}
