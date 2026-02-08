package com.samsara.polymath.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

            // Set card background to persona color
            val bgColor = try {
                Color.parseColor(report.persona.backgroundColor)
            } catch (_: Exception) {
                Color.WHITE
            }
            (binding.root as? com.google.android.material.card.MaterialCardView)?.setCardBackgroundColor(bgColor)

            // Dynamic text color based on persona background luminance
            val darkness = 1 - (0.299 * Color.red(bgColor) + 0.587 * Color.green(bgColor) + 0.114 * Color.blue(bgColor)) / 255
            val textColor = if (darkness >= 0.5) Color.WHITE else Color.BLACK
            val subtextColor = if (darkness >= 0.5) 0xFFB0BEC5.toInt() else 0xFF757575.toInt()
            binding.personaNameTextView.setTextColor(textColor)
            binding.openCountTextView.setTextColor(textColor)
            binding.completionRateTextView.setTextColor(textColor)
            binding.openCountChangeTextView.setTextColor(subtextColor)
            binding.completionRateChangeTextView.setTextColor(subtextColor)
            binding.tasksTextView.setTextColor(subtextColor)
            binding.opensLabelTextView.setTextColor(subtextColor)
            binding.completionLabelTextView.setTextColor(subtextColor)

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

            // Tags as circles on the right
            populateTagCircles(binding.tagsContainer, report.tags)
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
        fun populateTagCircles(container: LinearLayout, tags: List<Tag>) {
            container.removeAllViews()
            if (tags.isEmpty()) {
                container.visibility = View.GONE
                return
            }
            container.visibility = View.VISIBLE
            val sizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 28f, container.resources.displayMetrics
            ).toInt()
            val marginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, container.resources.displayMetrics
            ).toInt()

            for (tag in tags) {
                val tv = TextView(container.context).apply {
                    text = tag.name.firstOrNull()?.uppercase() ?: "?"
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        val bgColor = try {
                            tag.color?.let { Color.parseColor(it) } ?: 0xFF90A4AE.toInt()
                        } catch (_: Exception) {
                            0xFF90A4AE.toInt()
                        }
                        setColor(bgColor)
                    }
                    background = bg
                    layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                        marginStart = marginPx
                    }
                }
                container.addView(tv)
            }
        }
    }
}
