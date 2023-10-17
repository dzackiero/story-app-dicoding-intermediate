package com.pnj.storyapp.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pnj.storyapp.data.model.Story

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStory(story: Story)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStories(stories: List<Story>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, Story>

    @Query("DELETE FROM story")
    fun deleteAllStory()
}