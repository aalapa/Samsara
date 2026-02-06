package com.samsara.polymath.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.samsara.polymath.adapter.PersonaReportAdapter
import com.samsara.polymath.data.PersonaReport
import com.samsara.polymath.databinding.FragmentReportTimeBinding
import com.samsara.polymath.util.ReportUtils
import com.samsara.polymath.viewmodel.PersonaReportViewModel
import java.text.SimpleDateFormat
import java.util.*

class ReportTimeFragment : Fragment() {

    private var _binding: FragmentReportTimeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonaReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup accordion
        ReportUtils.setupAccordion(binding.timeSpentHeader, binding.timeSpentContent, binding.timeSpentArrow)

        // Heatmap day-click listener
        binding.heatmapView.onDayClickListener = { dayMillis ->
            viewModel.selectDay(dayMillis)
        }

        // Observe heatmap data
        viewModel.heatmapData.observe(viewLifecycleOwner) { data ->
            binding.heatmapView.setData(data)
        }

        // Observe selected day
        viewModel.selectedDay.observe(viewLifecycleOwner) { dayMillis ->
            binding.heatmapView.setSelectedDay(dayMillis)
        }

        // Observe day breakdown
        viewModel.selectedDayBreakdown.observe(viewLifecycleOwner) { breakdown ->
            val selectedDay = viewModel.selectedDay.value
            if (breakdown.isNullOrEmpty() || selectedDay == null) {
                binding.dayDetailCard.visibility = View.GONE
                return@observe
            }
            binding.dayDetailCard.visibility = View.VISIBLE

            val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            binding.dayDetailTitle.text = dateFormat.format(Date(selectedDay))

            binding.dayDetailContent.removeAllViews()
            val density = resources.displayMetrics.density

            for (entry in breakdown) {
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
                }

                val nameView = TextView(requireContext()).apply {
                    text = entry.personaName
                    setTextColor(Color.parseColor("#0D47A1"))
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val timeView = TextView(requireContext()).apply {
                    text = ReportUtils.formatDuration(entry.totalTime)
                    setTextColor(Color.parseColor("#1565C0"))
                    textSize = 14f
                    setTypeface(typeface, Typeface.BOLD)
                }

                row.addView(nameView)
                row.addView(timeView)
                binding.dayDetailContent.addView(row)
            }
        }

        // Observe report for time-spent section
        viewModel.reportSummary.observe(viewLifecycleOwner) { report ->
            populateTimeSpent(report.mostTimeSpent)
        }
    }

    private fun populateTimeSpent(reports: List<PersonaReport>) {
        binding.timeSpentContent.removeAllViews()
        if (reports.isEmpty()) {
            binding.timeSpentHeader.visibility = View.GONE
            return
        }
        binding.timeSpentHeader.visibility = View.VISIBLE

        val density = resources.displayMetrics.density
        for (report in reports) {
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (8 * density).toInt()
                }
                setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                cardElevation = 2 * density
            }

            val inner = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
                gravity = Gravity.CENTER_VERTICAL
            }

            val textLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val nameView = TextView(requireContext()).apply {
                text = report.persona.name
                setTextColor(Color.parseColor("#E65100"))
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
            }

            val statsView = TextView(requireContext()).apply {
                text = ReportUtils.formatDuration(report.totalTimeSpent)
                setTextColor(Color.parseColor("#F57C00"))
                textSize = 14f
            }

            textLayout.addView(nameView)
            textLayout.addView(statsView)
            inner.addView(textLayout)

            val tagsContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = (8 * density).toInt()
                }
                gravity = Gravity.CENTER
            }
            PersonaReportAdapter.populateTagCircles(tagsContainer, report.tags)
            inner.addView(tagsContainer)

            card.addView(inner)
            binding.timeSpentContent.addView(card)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
