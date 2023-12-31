package com.pnj.storyapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.pnj.storyapp.data.model.Story
import com.pnj.storyapp.databinding.StoryItemBinding
import com.pnj.storyapp.ui.detail.DetailActivity
import com.pnj.storyapp.util.loadImage

class StoryAdapter(
    private val dataset: List<Story>,
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(
        val binding: StoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = StoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StoryViewHolder(binding)
    }

    override fun getItemCount(): Int = dataset.size

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = dataset[position]

        holder.binding.apply {
            tvItemName.text = story.name
            tvItemDescription.text = story.description
            ivItemPhoto.loadImage(holder.itemView.context, story.photoUrl)
        }

        holder.apply {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(DetailActivity.ID_KEY, story.id)

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