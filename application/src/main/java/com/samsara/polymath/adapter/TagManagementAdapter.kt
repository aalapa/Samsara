package com.samsara.polymath.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.R
import com.samsara.polymath.data.TagWithUsageCount
import com.samsara.polymath.databinding.ItemTagManagementBinding

class TagManagementAdapter(
    private val onRenameTag: (TagWithUsageCount) -> Unit,
    private val onDeleteTag: (TagWithUsageCount) -> Unit
) : ListAdapter<TagWithUsageCount, TagManagementAdapter.TagViewHolder>(TagDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagManagementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TagViewHolder(
        private val binding: ItemTagManagementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tagWithUsage: TagWithUsageCount) {
            val tag = tagWithUsage.tag
            val usageCount = tagWithUsage.usageCount

            binding.tagNameTextView.text = tag.name

            // Display usage count
            val usageText = if (usageCount == 1) {
                "$usageCount persona"
            } else {
                "$usageCount personas"
            }
            binding.tagUsageTextView.text = usageText

            // Set color indicator
            try {
                val color = if (tag.color != null) {
                    Color.parseColor(tag.color)
                } else {
                    Color.parseColor("#666666")
                }
                binding.tagColorIndicator.setBackgroundColor(color)
            } catch (e: Exception) {
                binding.tagColorIndicator.setBackgroundColor(Color.parseColor("#666666"))
            }

            // Setup menu
            binding.menuButton.setOnClickListener { view ->
                val contextWrapper = android.view.ContextThemeWrapper(
                    view.context,
                    R.style.PopupMenuTheme
                )
                val popup = PopupMenu(contextWrapper, view)
                popup.menuInflater.inflate(R.menu.tag_menu, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_rename_tag -> {
                            onRenameTag(tagWithUsage)
                            true
                        }
                        R.id.action_delete_tag -> {
                            onDeleteTag(tagWithUsage)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    class TagDiffCallback : DiffUtil.ItemCallback<TagWithUsageCount>() {
        override fun areItemsTheSame(oldItem: TagWithUsageCount, newItem: TagWithUsageCount): Boolean {
            return oldItem.tag.id == newItem.tag.id
        }

        override fun areContentsTheSame(oldItem: TagWithUsageCount, newItem: TagWithUsageCount): Boolean {
            return oldItem == newItem
        }
    }
}

