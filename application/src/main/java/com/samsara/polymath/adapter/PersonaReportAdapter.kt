package com.samsara.polymath.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.samsara.polymath.R
import com.samsara.polymath.data.PersonaReport
import com.samsara.polymath.data.Tag
import com.samsara.polymath.data.TrendDirection
import com.samsara.polymath.databinding.ItemPersonaReportBinding

class PersonaReportAdapter : ListAdapter<PersonaReport, PersonaReportAdapter.PersonaReportViewHolder>(PersonaReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaReportViewHolder {
        val binding = ItemPersonaReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonaReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonaReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PersonaReportViewHolder(private val binding: ItemPersonaReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: PersonaReport) {
            binding.personaNameTextView.text = report.persona.name

            // Set overall trend indicator
            when (report.completionRateTrend) {
                TrendDirection.UP -> {
                    binding.trendIndicatorImageView.setImageResource(R.drawable.ic_rank_up)
                    binding.trendIndicatorImageView.visibility = View.VISIBLE
                }
                TrendDirection.DOWN -> {
                    binding.trendIndicatorImageView.setImageResource(R.drawable.ic_rank_down)
                    binding.trendIndicatorImageView.visibility = View.VISIBLE
                }
                TrendDirection.STABLE -> {
                    binding.trendIndicatorImageView.setImageResource(R.drawable.ic_rank_stable)
                    binding.trendIndicatorImageView.visibility = View.VISIBLE
                }
            }

            // Open count
            binding.openCountTextView.text = report.currentOpenCount.toString()
            val openCountChange = report.currentOpenCount - report.previousOpenCount

            if (openCountChange > 0) {
                binding.openCountTrendImageView.setImageResource(R.drawable.ic_rank_up)
                binding.openCountTrendImageView.visibility = View.VISIBLE
                binding.openCountChangeTextView.text = "(+$openCountChange)"
                binding.openCountChangeTextView.visibility = View.VISIBLE
            } else if (openCountChange < 0) {
                binding.openCountTrendImageView.setImageResource(R.drawable.ic_rank_down)
                binding.openCountTrendImageView.visibility = View.VISIBLE
                binding.openCountChangeTextView.text = "($openCountChange)"
                binding.openCountChangeTextView.visibility = View.VISIBLE
            } else {
                binding.openCountTrendImageView.visibility = View.GONE
                binding.openCountChangeTextView.visibility = View.GONE
            }

            // Completion rate
            val completionRatePercentage = (report.currentCompletionRate * 100).toInt()
            binding.completionRateTextView.text = "$completionRatePercentage%"

            val completionRateChange = ((report.currentCompletionRate - report.previousCompletionRate) * 100).toInt()

            if (completionRateChange > 0) {
                binding.completionRateTrendImageView.setImageResource(R.drawable.ic_rank_up)
                binding.completionRateTrendImageView.visibility = View.VISIBLE
                binding.completionRateChangeTextView.text = "(+$completionRateChange%)"
                binding.completionRateChangeTextView.visibility = View.VISIBLE
            } else if (completionRateChange < 0) {
                binding.completionRateTrendImageView.setImageResource(R.drawable.ic_rank_down)
                binding.completionRateTrendImageView.visibility = View.VISIBLE
                binding.completionRateChangeTextView.text = "($completionRateChange%)"
                binding.completionRateChangeTextView.visibility = View.VISIBLE
            } else {
                binding.completionRateTrendImageView.visibility = View.GONE
                binding.completionRateChangeTextView.visibility = View.GONE
            }

            // Tasks summary
            binding.tasksTextView.text = "${report.completedTasks} completed / ${report.totalTasks} total tasks"

            // Tags
            populateTags(binding.tagsChipGroup, report.tags)
        }
    }

    class PersonaReportDiffCallback : DiffUtil.ItemCallback<PersonaReport>() {
        override fun areItemsTheSame(oldItem: PersonaReport, newItem: PersonaReport): Boolean {
            return oldItem.persona.id == newItem.persona.id
        }

        override fun areContentsTheSame(oldItem: PersonaReport, newItem: PersonaReport): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        fun populateTags(chipGroup: ChipGroup, tags: List<Tag>) {
            chipGroup.removeAllViews()
            if (tags.isEmpty()) {
                chipGroup.visibility = View.GONE
                return
            }
            chipGroup.visibility = View.VISIBLE
            for (tag in tags) {
                val chip = Chip(chipGroup.context).apply {
                    text = tag.name
                    isClickable = false
                    isCheckable = false
                    textSize = 10f
                    chipMinHeight = 24f
                    ensureAccessibleTouchTarget(0)
                    tag.color?.let { colorStr ->
                        try {
                            val color = Color.parseColor(colorStr)
                            chipBackgroundColor = android.content.res.ColorStateList.valueOf(color)
                            // Use white text on dark backgrounds
                            val luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
                            setTextColor(if (luminance < 0.5) Color.WHITE else Color.BLACK)
                        } catch (_: Exception) { }
                    }
                }
                chipGroup.addView(chip)
            }
        }
    }
}
