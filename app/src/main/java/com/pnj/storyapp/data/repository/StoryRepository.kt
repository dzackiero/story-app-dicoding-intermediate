package com.pnj.storyapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.pnj.storyapp.data.StoryRemoteMediator
import com.pnj.storyapp.data.database.StoryDatabase
import com.pnj.storyapp.data.model.Story
import com.pnj.storyapp.data.model.UserModel
import com.pnj.storyapp.data.pref.UserPreferences
import com.pnj.storyapp.data.response.LoginResponse
import com.pnj.storyapp.data.response.MessageResponse
import com.pnj.storyapp.data.response.StoryResponse
import com.pnj.storyapp.data.retrofit.ApiService
import com.pnj.storyapp.util.Result
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val apiService: ApiService,
    private val pref: UserPreferences,
    private val database: StoryDatabase,
) {

    fun uploadImage(
        token: String,
        imageUri: MultipartBody.Part,
        description: RequestBody
    ): LiveData<Result<MessageResponse>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.uploadStory("bearer $token", imageUri, description)
                if (response.error) {
                    emit(Result.Error("Upload Error: ${response.message}"))
                    Log.d("Upload Error", response.message)
                } else {
                    emit(Result.Success(response))
                    Log.d("Upload Success", response.message)
                }
            } catch (e: Exception) {
                emit(Result.Error("Error : ${e.message.toString()}"))
                Log.d("Upload Exception", e.message.toString())
            }
        }

    fun getDetailStory(
        token: String,
        id: String
    ): LiveData<Result<StoryResponse>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.getDetailStory("Bearer $token", id)
                if (response.error) {
                    emit(Result.Error("Detail Error: ${response.message}"))
                    Log.d("Detail Error", response.message)
                } else {
                    emit(Result.Success(response))
                    Log.d("Detail Success", response.message)
                }
            } catch (e: Exception) {
                emit(Result.Error("Error : ${e.message.toString()}"))
                Log.d("Detail Exception", e.message.toString())
            }
        }

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(
        token: String
    ): LiveData<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(database, apiService, token),
            pagingSourceFactory = {
                database.storyDao().getAllStory()
            }
        ).liveData
    }

    fun getSession() = pref.getSession()

    suspend fun logout() {
        pref.logout()
    }

    fun registerUser(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<String>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.register(name, email, password)
                if (response.error) {
                    emit(Result.Error("Register Error: ${response.message}"))
                    Log.d("Register Error", response.message)
                } else {
                    emit(Result.Success("User Created"))
                    Log.d("Register Success", response.message)
                }
            } catch (e: Exception) {
                emit(Result.Error("Error : ${e.message.toString()}"))
                Log.d("Register Exception", e.message.toString())
            }
        }

    fun loginUser(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.login(email, password)
                if (response.error) {
                    Log.d("Login Error", response.message)
                    emit(Result.Error("Login Error: ${response.message}"))
                } else {
                    Log.d("Login Success", response.message)
                    emit(Result.Success(response))

                    pref.saveSession(
                        UserModel(
                            response.loginResult.userId,
                            response.loginResult.name,
                            response.loginResult.token,
                            true
                        )
                    )
                }
            } catch (e: Exception) {
                Log.d("Login Exception", e.message.toString())
                emit(Result.Error("Error : ${e.message.toString()}"))
            }
        }

    fun getThemeSetting(): Flow<Boolean> =
        pref.getThemeSetting()

    suspend fun saveThemeSetting(isNightModeActive: Boolean) {
        pref.saveThemeSetting(isNightModeActive)
    }

    companion object {
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            pref: UserPreferences,
            database: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, pref, database)
            }.also { instance = it }
    }
}
