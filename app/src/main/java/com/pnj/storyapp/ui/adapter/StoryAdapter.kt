package com.pnj.storyapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pnj.storyapp.data.model.Story
import com.pnj.storyapp.databinding.StoryItemBinding
import com.pnj.storyapp.util.loadImage

class StoryAdapter(
    private val dataset: List<Story>,
    private val listener: ((Story) -> Unit)? = null
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

        listener?.let {
            holder.itemView.setOnClickListener { it(story) }
        }
    }
}