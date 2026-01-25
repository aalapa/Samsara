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
import com.samsara.polymath.data.DecayLevel
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
            
            // Display persona name with completed task count prefix
            val displayText = "$completedCount ${persona.name}"
            binding.personaNameTextView.text = displayText
            
            // Helper function to determine if color is dark
            fun isColorDark(color: Int): Boolean {
                val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
                return darkness >= 0.5
            }
            
            // Helper function to desaturate a color (0.0 = grayscale, 1.0 = full color)
            fun desaturateColor(color: Int, saturation: Float): Int {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                val newR = (gray + saturation * (r - gray)).toInt().coerceIn(0, 255)
                val newG = (gray + saturation * (g - gray)).toInt().coerceIn(0, 255)
                val newB = (gray + saturation * (b - gray)).toInt().coerceIn(0, 255)
                return Color.rgb(newR, newG, newB)
            }
            
            // Parse original background color
            var bgColor = Color.parseColor("#FFFFFF")
            try {
                bgColor = Color.parseColor(persona.backgroundColor)
            } catch (e: Exception) {
                // Fallback to white if parsing fails
                bgColor = Color.parseColor("#FFFFFF")
            }

            // Calculate FINAL background color after applying decay
            val finalBgColor = when (personaWithCount.decayLevel) {
                DecayLevel.SERIOUS -> desaturateColor(bgColor, 0.3f)
                else -> bgColor
            }
            
            // Calculate text color based on FINAL background color
            val textColor = if (isColorDark(finalBgColor)) Color.WHITE else Color.BLACK
            val menuIconColor = if (isColorDark(finalBgColor)) Color.WHITE else Color.BLACK
            
            // Apply colors to UI
            binding.personaNameTextView.setTextColor(textColor)
            binding.openCountTextView.setTextColor(textColor)
            binding.menuButton.setColorFilter(menuIconColor)
            
            // Show score (rounded to integer) if greater than 0
            // Score = (1 + completedTasks/totalTasks) × openCount × decayMultiplier
            val score = personaWithCount.score
            if (score > 0) {
                binding.openCountTextView.text = score.toInt().toString()
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

            // Apply visual decay based on decay level
            applyDecayVisuals(personaWithCount.decayLevel, finalBgColor)

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
            
            // Keep tags hidden to maintain compact card size
            binding.tagsChipGroup.visibility = View.GONE
        }

        /**
         * Apply visual decay effects based on decay level.
         * - NONE: No effect (100% opacity, full color)
         * - SLIGHT: 90% opacity
         * - MEDIUM: 75% opacity
         * - SERIOUS: 60% opacity + desaturated background color
         */
        private fun applyDecayVisuals(decayLevel: DecayLevel, bgColor: Int) {
            when (decayLevel) {
                DecayLevel.NONE -> {
                    binding.root.alpha = 1.0f
                    binding.root.setCardBackgroundColor(bgColor)
                }
                DecayLevel.SLIGHT -> {
                    binding.root.alpha = 0.90f
                    binding.root.setCardBackgroundColor(bgColor)
                }
                DecayLevel.MEDIUM -> {
                    binding.root.alpha = 0.75f
                    binding.root.setCardBackgroundColor(bgColor)
                }
                DecayLevel.SERIOUS -> {
                    binding.root.alpha = 0.60f
                    // Apply desaturation to the background color
                    binding.root.setCardBackgroundColor(desaturateColor(bgColor, 0.3f))
                }
            }
        }

        /**
         * Desaturate a color by a given factor (0.0 = grayscale, 1.0 = full color)
         */
        private fun desaturateColor(color: Int, saturation: Float): Int {
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            // Calculate grayscale value using luminance formula
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            // Blend between grayscale and original color
            val newR = (gray + saturation * (r - gray)).toInt().coerceIn(0, 255)
            val newG = (gray + saturation * (g - gray)).toInt().coerceIn(0, 255)
            val newB = (gray + saturation * (b - gray)).toInt().coerceIn(0, 255)

            return Color.rgb(newR, newG, newB)
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
                && oldItem.score == newItem.score
                && oldItem.rankStatus == newItem.rankStatus
                && oldItem.decayLevel == newItem.decayLevel
        }
    }
}

