package com.pnj.storyapp.di

import android.content.Context
import com.pnj.storyapp.data.pref.UserPreferences
import com.pnj.storyapp.data.pref.dataStore
import com.pnj.storyapp.data.repository.StoryRepository
import com.pnj.storyapp.data.retrofit.RetrofitFactory

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val apiService = RetrofitFactory.makeRetrofitService()
        val pref = UserPreferences.getInstance(context.dataStore)
        return StoryRepository.getInstance(apiService, pref)
    }
}