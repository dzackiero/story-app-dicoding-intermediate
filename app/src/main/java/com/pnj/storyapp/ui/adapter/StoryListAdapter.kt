package com.pnj.storyapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pnj.storyapp.data.model.Story
import com.pnj.storyapp.databinding.StoryItemBinding
import com.pnj.storyapp.ui.detail.DetailActivity
import com.pnj.storyapp.util.loadImage

class StoryListAdapter
    : PagingDataAdapter<Story, StoryListAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    inner class StoryViewHolder(
        val binding: StoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        story?.let {
            holder.binding.apply {
                tvItemName.text = story.name
                tvItemDescription.text = story.description
                ivItemPhoto.loadImage(holder.itemView.context, story.photoUrl)
            }

            holder.apply {
                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.STORY_KEY, story)

                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(binding.tvItemName as View, "name"),
                            Pair(binding.tvItemDescription as View, "description"),
                            Pair(binding.ivItemPhoto as View, "photo")
                        )

                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}