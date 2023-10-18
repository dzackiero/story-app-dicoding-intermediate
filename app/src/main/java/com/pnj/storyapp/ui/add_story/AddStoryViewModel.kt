package com.pnj.storyapp.ui.add_story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.data.repository.StoryRepository
import com.pnj.storyapp.data.response.MessageResponse
import com.pnj.storyapp.util.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryViewModel(private val mStoryRepository: StoryRepository) : ViewModel() {
    fun uploadImage(
        token: String,
        imageFile: File,
        description: String,
        lat: Double? = null,
        lon: Double? = null,
    ): LiveData<Result<MessageResponse>> {
        val requestDesc = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        val requestLat: RequestBody? = lat?.toString()?.toRequestBody()
        val requestLon: RequestBody? = lon?.toString()?.toRequestBody()
        return mStoryRepository.uploadImage(
            token,
            multipartBody,
            requestDesc,
            requestLat,
            requestLon
        )
    }


    fun getSessionData(): LiveData<UserModel> =
        mStoryRepository.getSession().asLiveData()
}
