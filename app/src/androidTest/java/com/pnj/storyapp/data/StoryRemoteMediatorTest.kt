package com.pnj.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pnj.storyapp.data.database.StoryDatabase
import com.pnj.storyapp.data.model.Story
import com.pnj.storyapp.data.response.LoginResponse
import com.pnj.storyapp.data.response.LoginResult
import com.pnj.storyapp.data.response.MessageResponse
import com.pnj.storyapp.data.response.StoriesResponse
import com.pnj.storyapp.data.response.StoryResponse
import com.pnj.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.toImmutableList
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@ExperimentalPagingApi
@RunWith(AndroidJUnit4::class)
class StoryRemoteMediatorTest {
    private var mockApi: ApiService = FakeApiService()
    private var mockDb: StoryDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        StoryDatabase::class.java
    ).allowMainThreadQueries().build()

    @After
    fun tearDown() {
        mockDb.clearAllTables()
    }

    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        val remoteMediator = StoryRemoteMediator(
            mockDb,
            mockApi,
            "token"
        )
        val pagingState = PagingState<Int, Story>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }
}

class FakeApiService : ApiService {
    override suspend fun register(name: String, email: String, password: String): MessageResponse {
        return MessageResponse(false, "")
    }

    override suspend fun login(email: String, password: String): LoginResponse {
        return LoginResponse(false, "", LoginResult("", "", ""))
    }

    override suspend fun getStories(token: String, page: Int, size: Int): StoriesResponse {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                i.toString(),
                "https://alumni.engineering.utoronto.ca/files/2022/05/Avatar-Placeholder-400x400-1-300x300.jpg",
                LocalDate.now().toString(),
                "Testing Name",
                "Lorem Ipsum Desc",
                1.0,
                1.0,
            )
            items.add(story)
        }
        return StoriesResponse(
            items.subList((page - 1) * size, (page - 1) * size + size).toImmutableList(),
            false,
            ""
        )
    }

    override suspend fun getStoriesWithLocation(
        token: String,
        size: Int,
        location: Int
    ): StoriesResponse {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                i.toString(),
                "https://alumni.engineering.utoronto.ca/files/2022/05/Avatar-Placeholder-400x400-1-300x300.jpg",
                LocalDate.now().toString(),
                "Testing Name",
                "Lorem Ipsum Desc",
                1.0,
                1.0,
            )
            items.add(story)
        }
        return StoriesResponse(
            items.toImmutableList(),
            false,
            ""
        )
    }

    override suspend fun getDetailStory(token: String, id: String): StoryResponse {
        return StoryResponse(
            false,
            "",
            Story("", "", "", "", "", 0.0, 0.0)
        )
    }

    override suspend fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): MessageResponse {
        return MessageResponse(false, "")
    }

}
