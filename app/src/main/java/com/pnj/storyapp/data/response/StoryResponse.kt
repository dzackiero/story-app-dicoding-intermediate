package com.pnj.storyapp.data.response

import com.pnj.storyapp.data.model.Story

data class StoryResponse(
    val error: Boolean,
    val message: String,
    val story: Story
)

