package com.samsara.polymath.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.samsara.polymath.adapter.PersonaReportAdapter
import com.samsara.polymath.data.PersonaReport
import com.samsara.polymath.data.ReportType
import com.samsara.polymath.databinding.FragmentReportOverviewBinding
import com.samsara.polymath.util.ReportUtils
import com.samsara.polymath.viewmodel.PersonaReportViewModel

class ReportOverviewFragment : Fragment() {

    private var _binding: FragmentReportOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonaReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup accordions
        ReportUtils.setupAccordion(binding.mostActiveHeader, binding.mostActiveContent, binding.mostActiveArrow)
        ReportUtils.setupAccordion(binding.mostImprovedHeader, binding.mostImprovedContent, binding.mostImprovedArrow)
        ReportUtils.setupAccordion(binding.needsAttentionHeader, binding.needsAttentionContent, binding.needsAttentionArrow)

        // Observe report data
        viewModel.reportSummary.observe(viewLifecycleOwner) { report ->
            populateOverview(report)
        }
    }

    private fun populateOverview(report: com.samsara.polymath.data.ReportSummary) {
        // Most Active
        bindHighlightCard(report.mostActive.getOrNull(0), binding.mostActiveCard1, binding.mostActive1PersonaName, binding.mostActive1PersonaStats, binding.mostActive1Tags) { r -> "Opened ${r.currentOpenCount} times" }
        bindHighlightCard(report.mostActive.getOrNull(1), binding.mostActiveCard2, binding.mostActive2PersonaName, binding.mostActive2PersonaStats, binding.mostActive2Tags) { r -> "Opened ${r.currentOpenCount} times" }
        binding.mostActiveHeader.visibility = if (report.mostActive.isEmpty()) View.GONE else View.VISIBLE

        // Most Improved
        bindHighlightCard(report.mostImproved.getOrNull(0), binding.mostImprovedCard1, binding.mostImproved1PersonaName, binding.mostImproved1PersonaStats, binding.mostImproved1Tags) { r ->
            val curr = (r.currentCompletionRate * 100).toInt()
            val prev = (r.previousCompletionRate * 100).toInt()
            "Completion rate: $prev% → $curr% (+${curr - prev}%)"
        }
        bindHighlightCard(report.mostImproved.getOrNull(1), binding.mostImprovedCard2, binding.mostImproved2PersonaName, binding.mostImproved2PersonaStats, binding.mostImproved2Tags) { r ->
            val curr = (r.currentCompletionRate * 100).toInt()
            val prev = (r.previousCompletionRate * 100).toInt()
            "Completion rate: $prev% → $curr% (+${curr - prev}%)"
        }
        binding.mostImprovedHeader.visibility = if (report.mostImproved.isEmpty()) View.GONE else View.VISIBLE

        // Needs Attention
        bindHighlightCard(report.needsAttention.getOrNull(0), binding.needsAttentionCard1, binding.needsAttention1PersonaName, binding.needsAttention1PersonaStats, binding.needsAttention1Tags) { r -> getNeedsAttentionMessage(r) }
        bindHighlightCard(report.needsAttention.getOrNull(1), binding.needsAttentionCard2, binding.needsAttention2PersonaName, binding.needsAttention2PersonaStats, binding.needsAttention2Tags) { r -> getNeedsAttentionMessage(r) }
        binding.needsAttentionHeader.visibility = if (report.needsAttention.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun bindHighlightCard(
        report: PersonaReport?,
        card: View,
        nameView: TextView,
        statsView: TextView,
        tagsContainer: LinearLayout,
        statsFormatter: (PersonaReport) -> String
    ) {
        if (report == null) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE
        nameView.text = report.persona.name
        statsView.text = statsFormatter(report)
        PersonaReportAdapter.populateTagCircles(tagsContainer, report.tags)
    }

    private fun getNeedsAttentionMessage(report: PersonaReport): String {
        return when {
            report.currentOpenCount == 0 -> {
                val days = when (viewModel.getCurrentReportType()) {
                    ReportType.WEEKLY -> "7 days"
                    ReportType.MONTHLY -> "30 days"
                }
                "Not opened in $days"
            }
            report.currentCompletionRate < 0.3 -> {
                "Low completion rate: ${(report.currentCompletionRate * 100).toInt()}%"
            }
            else -> "Needs more attention"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
