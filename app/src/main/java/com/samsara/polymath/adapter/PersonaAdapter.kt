package com.samsara.polymath.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.samsara.polymath.R
import com.samsara.polymath.data.Persona
import com.samsara.polymath.data.PersonaWithTaskCount
import com.samsara.polymath.data.RankStatus
import com.samsara.polymath.databinding.ItemPersonaBinding

class PersonaAdapter(
    private val onPersonaClick: (Persona) -> Unit,
    private val onPersonaEdit: (Persona) -> Unit,
    private val onPersonaDelete: (Persona) -> Unit
) : ListAdapter<PersonaWithTaskCount, PersonaAdapter.PersonaViewHolder>(PersonaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaViewHolder {
        val binding = ItemPersonaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonaViewHolder(
        private val binding: ItemPersonaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(personaWithCount: PersonaWithTaskCount) {
            val persona = personaWithCount.persona
            val completedCount = personaWithCount.completedTaskCount
            val emoji = personaWithCount.emoji
            
            // Display persona name with completed task count prefix and emoji
            val displayText = if (emoji.isNotEmpty()) {
                "$emoji $completedCount ${persona.name}"
            } else {
                "$completedCount ${persona.name}"
            }
            binding.personaNameTextView.text = displayText
            
            // Helper function to determine if color is dark
            fun isColorDark(color: Int): Boolean {
                val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
                return darkness >= 0.5
            }
            
            // Apply background and text colors
            try {
                val bgColor = Color.parseColor(persona.backgroundColor)
                val textColor = Color.parseColor(persona.textColor)
                binding.root.setCardBackgroundColor(bgColor)
                binding.personaNameTextView.setTextColor(textColor)
                binding.openCountTextView.setTextColor(textColor)
                // Menu icon should be visible - use text color or white/black based on background
                val menuIconColor = if (isColorDark(bgColor)) Color.WHITE else Color.BLACK
                binding.menuButton.setColorFilter(menuIconColor)
            } catch (e: Exception) {
                // Fallback to default colors if parsing fails
                binding.root.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                binding.personaNameTextView.setTextColor(Color.parseColor("#000000"))
                binding.menuButton.setColorFilter(Color.parseColor("#8E8E93"))
            }
            
            // Show open count if greater than 0
            if (persona.openCount > 0) {
                binding.openCountTextView.text = persona.openCount.toString()
                binding.openCountTextView.visibility = View.VISIBLE
            } else {
                binding.openCountTextView.visibility = View.GONE
            }

            // Set rank indicator icon based on rank status
            val rankStatus = personaWithCount.rankStatus
            when (rankStatus) {
                RankStatus.UP -> {
                    binding.rankIndicatorImageView.setImageResource(R.drawable.ic_persona_rank_up)
                    binding.rankIndicatorImageView.visibility = View.VISIBLE
                }
                RankStatus.DOWN -> {
                    binding.rankIndicatorImageView.setImageResource(R.drawable.ic_persona_rank_down)
                    binding.rankIndicatorImageView.visibility = View.VISIBLE
                }
                RankStatus.STABLE -> {
                    binding.rankIndicatorImageView.visibility = View.GONE
                }
            }
            
            // Setup three dots menu
            binding.menuButton.setOnClickListener { view ->
                val contextWrapper = android.view.ContextThemeWrapper(
                    view.context,
                    R.style.PopupMenuTheme
                )
                val popup = PopupMenu(contextWrapper, view)
                popup.menuInflater.inflate(R.menu.persona_menu, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit_persona -> {
                            onPersonaEdit(persona)
                            true
                        }
                        R.id.action_delete_persona -> {
                            onPersonaDelete(persona)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
            
            binding.root.setOnClickListener {
                onPersonaClick(persona)
            }
        }
    }

    class PersonaDiffCallback : DiffUtil.ItemCallback<PersonaWithTaskCount>() {
        override fun areItemsTheSame(oldItem: PersonaWithTaskCount, newItem: PersonaWithTaskCount): Boolean {
            return oldItem.persona.id == newItem.persona.id
        }

        override fun areContentsTheSame(oldItem: PersonaWithTaskCount, newItem: PersonaWithTaskCount): Boolean {
            return oldItem.persona == newItem.persona
                && oldItem.completedTaskCount == newItem.completedTaskCount
                && oldItem.openTaskCount == newItem.openTaskCount
                && oldItem.emoji == newItem.emoji
                && oldItem.score == newItem.score
                && oldItem.rankStatus == newItem.rankStatus
        }
    }
}

