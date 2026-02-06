package com.samsara.polymath.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.samsara.polymath.adapter.PersonaReportAdapter
import com.samsara.polymath.data.ReportSummary
import com.samsara.polymath.data.TagReport
import com.samsara.polymath.databinding.FragmentReportTrendsBinding
import com.samsara.polymath.util.ReportUtils
import com.samsara.polymath.viewmodel.PersonaReportViewModel

class ReportTrendsFragment : Fragment() {

    private var _binding: FragmentReportTrendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonaReportViewModel by activityViewModels()
    private lateinit var adapter: PersonaReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = PersonaReportAdapter()
        binding.personaReportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.personaReportsRecyclerView.adapter = adapter

        // Setup accordions
        ReportUtils.setupAccordion(binding.tagInsightsHeader, binding.tagInsightsContent, binding.tagInsightsArrow)
        ReportUtils.setupAccordion(binding.allPersonasHeader, binding.personaReportsRecyclerView, binding.allPersonasArrow)

        // Observe report data
        viewModel.reportSummary.observe(viewLifecycleOwner) { report ->
            populateTrends(report)
        }
    }

    private fun populateTrends(report: ReportSummary) {
        // Tag Insights
        val hasTagInsights = report.tagsMostActive.isNotEmpty() || report.tagsMostImproved.isNotEmpty() || report.tagsNeedAttention.isNotEmpty()
        binding.tagInsightsHeader.visibility = if (hasTagInsights) View.VISIBLE else View.GONE

        bindTagCard(report.tagsMostActive.getOrNull(0), binding.tagMostActiveCard1, binding.tagMostActive1Name, binding.tagMostActive1Stats) { t -> "${t.personaCount} personas, avg ${String.format("%.1f", t.avgOpenCount)} opens" }
        bindTagCard(report.tagsMostActive.getOrNull(1), binding.tagMostActiveCard2, binding.tagMostActive2Name, binding.tagMostActive2Stats) { t -> "${t.personaCount} personas, avg ${String.format("%.1f", t.avgOpenCount)} opens" }
        binding.tagMostActiveLabel.visibility = if (report.tagsMostActive.isEmpty()) View.GONE else View.VISIBLE

        bindTagCard(report.tagsMostImproved.getOrNull(0), binding.tagMostImprovedCard1, binding.tagMostImproved1Name, binding.tagMostImproved1Stats) { t ->
            val prev = (t.avgPreviousCompletionRate * 100).toInt()
            val curr = (t.avgCompletionRate * 100).toInt()
            "${t.personaCount} personas, completion: $prev% → $curr%"
        }
        bindTagCard(report.tagsMostImproved.getOrNull(1), binding.tagMostImprovedCard2, binding.tagMostImproved2Name, binding.tagMostImproved2Stats) { t ->
            val prev = (t.avgPreviousCompletionRate * 100).toInt()
            val curr = (t.avgCompletionRate * 100).toInt()
            "${t.personaCount} personas, completion: $prev% → $curr%"
        }
        binding.tagMostImprovedLabel.visibility = if (report.tagsMostImproved.isEmpty()) View.GONE else View.VISIBLE

        bindTagCard(report.tagsNeedAttention.getOrNull(0), binding.tagNeedAttentionCard1, binding.tagNeedAttention1Name, binding.tagNeedAttention1Stats) { t ->
            val completion = (t.avgCompletionRate * 100).toInt()
            if (t.avgOpenCount < 1.0) "${t.personaCount} personas, not opened"
            else "${t.personaCount} personas, avg completion: $completion%"
        }
        bindTagCard(report.tagsNeedAttention.getOrNull(1), binding.tagNeedAttentionCard2, binding.tagNeedAttention2Name, binding.tagNeedAttention2Stats) { t ->
            val completion = (t.avgCompletionRate * 100).toInt()
            if (t.avgOpenCount < 1.0) "${t.personaCount} personas, not opened"
            else "${t.personaCount} personas, avg completion: $completion%"
        }
        binding.tagNeedAttentionLabel.visibility = if (report.tagsNeedAttention.isEmpty()) View.GONE else View.VISIBLE

        // All Personas
        adapter.submitList(report.personaReports)
    }

    private fun bindTagCard(
        tagReport: TagReport?,
        card: View,
        nameView: TextView,
        statsView: TextView,
        statsFormatter: (TagReport) -> String
    ) {
        if (tagReport == null) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE
        nameView.text = tagReport.tag.name
        statsView.text = statsFormatter(tagReport)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
