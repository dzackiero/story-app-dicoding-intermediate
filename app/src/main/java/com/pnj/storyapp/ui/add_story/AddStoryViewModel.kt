package com.pnj.storyapp.ui.add_story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.data.repository.StoryRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val mStoryRepository: StoryRepository) : ViewModel() {
    fun uploadImage(
        token: String,
        imageUri: MultipartBody.Part,
        description: RequestBody
    ) = mStoryRepository.uploadImage(token, imageUri, description)
    
    fun getSessionData(): LiveData<UserModel> =
        mStoryRepository.getSession().asLiveData()
}
