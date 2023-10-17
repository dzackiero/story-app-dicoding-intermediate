package com.pnj.storyapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pnj.storyapp.data.model.Story

@Database(entities = [Story::class, RemoteKeys::class], version = 1, exportSchema = false)
abstract class StoryDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var database: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return database ?: synchronized(this) {
                database ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java,
                    "story_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { database = it }
            }
        }
    }
}