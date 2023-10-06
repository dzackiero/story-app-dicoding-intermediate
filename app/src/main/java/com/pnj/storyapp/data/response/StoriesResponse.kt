package com.pnj.storyapp.data.response

import com.pnj.storyapp.data.model.Story

data class StoriesResponse(
    val listStory: List<Story>,
    val error: Boolean,
    val message: String
)

