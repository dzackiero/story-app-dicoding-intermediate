package com.pnj.storyapp

import com.pnj.storyapp.data.model.Story
import java.time.LocalDate

object DataDummy {
    fun generateDummyStories(): List<Story> {
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
        return items
    }
}