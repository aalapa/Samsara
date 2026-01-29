package com.samsara.polymath

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.samsara.polymath.adapter.PersonaReportAdapter
import com.samsara.polymath.data.PersonaReport
import com.samsara.polymath.data.ReportType
import com.samsara.polymath.data.TagReport
import com.samsara.polymath.databinding.ActivityPersonaReportBinding
import com.samsara.polymath.viewmodel.PersonaReportViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PersonaReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonaReportBinding
    private lateinit var viewModel: PersonaReportViewModel
    private lateinit var adapter: PersonaReportAdapter
    private var currentReportType = ReportType.WEEKLY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonaReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[PersonaReportViewModel::class.java]

        // Setup RecyclerView
        adapter = PersonaReportAdapter()
        binding.personaReportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.personaReportsRecyclerView.adapter = adapter

        // Setup button listeners
        binding.weeklyButton.setOnClickListener {
            currentReportType = ReportType.WEEKLY
            updateButtonStates()
            loadReport()
        }

        binding.monthlyButton.setOnClickListener {
            currentReportType = ReportType.MONTHLY
            updateButtonStates()
            loadReport()
        }

        // Initial state
        updateButtonStates()
        loadReport()

        // Save current statistics for future comparisons
        viewModel.saveCurrentStatistics()
    }

    private fun updateButtonStates() {
        when (currentReportType) {
            ReportType.WEEKLY -> {
                binding.weeklyButton.isEnabled = false
                binding.monthlyButton.isEnabled = true
            }
            ReportType.MONTHLY -> {
                binding.weeklyButton.isEnabled = true
                binding.monthlyButton.isEnabled = false
            }
        }
    }

    private fun loadReport() {
        lifecycleScope.launch {
            try {
                val report = viewModel.generateReport(currentReportType)

                // Update date range
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val startDate = dateFormat.format(Date(report.startDate))
                val endDate = dateFormat.format(Date(report.endDate))
                binding.dateRangeTextView.text = "$startDate - $endDate"

                // Most Active highlights (top 2)
                bindHighlightCard(
                    report.mostActive.getOrNull(0),
                    binding.mostActiveCard1,
                    binding.mostActive1PersonaName,
                    binding.mostActive1PersonaStats,
                    binding.mostActive1Tags
                ) { r -> "Opened ${r.currentOpenCount} times" }

                bindHighlightCard(
                    report.mostActive.getOrNull(1),
                    binding.mostActiveCard2,
                    binding.mostActive2PersonaName,
                    binding.mostActive2PersonaStats,
                    binding.mostActive2Tags
                ) { r -> "Opened ${r.currentOpenCount} times" }

                // Hide section label if no cards
                binding.mostActiveLabel.visibility = if (report.mostActive.isEmpty()) View.GONE else View.VISIBLE

                // Most Improved highlights (top 2)
                bindHighlightCard(
                    report.mostImproved.getOrNull(0),
                    binding.mostImprovedCard1,
                    binding.mostImproved1PersonaName,
                    binding.mostImproved1PersonaStats,
                    binding.mostImproved1Tags
                ) { r ->
                    val currentRate = (r.currentCompletionRate * 100).toInt()
                    val previousRate = (r.previousCompletionRate * 100).toInt()
                    val change = currentRate - previousRate
                    "Completion rate: $previousRate% → $currentRate% (+$change%)"
                }

                bindHighlightCard(
                    report.mostImproved.getOrNull(1),
                    binding.mostImprovedCard2,
                    binding.mostImproved2PersonaName,
                    binding.mostImproved2PersonaStats,
                    binding.mostImproved2Tags
                ) { r ->
                    val currentRate = (r.currentCompletionRate * 100).toInt()
                    val previousRate = (r.previousCompletionRate * 100).toInt()
                    val change = currentRate - previousRate
                    "Completion rate: $previousRate% → $currentRate% (+$change%)"
                }

                binding.mostImprovedLabel.visibility = if (report.mostImproved.isEmpty()) View.GONE else View.VISIBLE

                // Needs Attention highlights (top 2)
                bindHighlightCard(
                    report.needsAttention.getOrNull(0),
                    binding.needsAttentionCard1,
                    binding.needsAttention1PersonaName,
                    binding.needsAttention1PersonaStats,
                    binding.needsAttention1Tags
                ) { r -> getNeedsAttentionMessage(r) }

                bindHighlightCard(
                    report.needsAttention.getOrNull(1),
                    binding.needsAttentionCard2,
                    binding.needsAttention2PersonaName,
                    binding.needsAttention2PersonaStats,
                    binding.needsAttention2Tags
                ) { r -> getNeedsAttentionMessage(r) }

                binding.needsAttentionLabel.visibility = if (report.needsAttention.isEmpty()) View.GONE else View.VISIBLE

                // Tag Insights
                val hasTagInsights = report.tagsMostActive.isNotEmpty() ||
                        report.tagsMostImproved.isNotEmpty() ||
                        report.tagsNeedAttention.isNotEmpty()
                binding.tagInsightsLabel.visibility = if (hasTagInsights) View.VISIBLE else View.GONE

                // Tags Most Active
                bindTagCard(
                    report.tagsMostActive.getOrNull(0),
                    binding.tagMostActiveCard1,
                    binding.tagMostActive1Name,
                    binding.tagMostActive1Stats
                ) { t -> "${t.personaCount} personas, avg ${String.format("%.1f", t.avgOpenCount)} opens" }

                bindTagCard(
                    report.tagsMostActive.getOrNull(1),
                    binding.tagMostActiveCard2,
                    binding.tagMostActive2Name,
                    binding.tagMostActive2Stats
                ) { t -> "${t.personaCount} personas, avg ${String.format("%.1f", t.avgOpenCount)} opens" }

                binding.tagMostActiveLabel.visibility = if (report.tagsMostActive.isEmpty()) View.GONE else View.VISIBLE

                // Tags Most Improved
                bindTagCard(
                    report.tagsMostImproved.getOrNull(0),
                    binding.tagMostImprovedCard1,
                    binding.tagMostImproved1Name,
                    binding.tagMostImproved1Stats
                ) { t ->
                    val prev = (t.avgPreviousCompletionRate * 100).toInt()
                    val curr = (t.avgCompletionRate * 100).toInt()
                    "${t.personaCount} personas, completion: $prev% → $curr%"
                }

                bindTagCard(
                    report.tagsMostImproved.getOrNull(1),
                    binding.tagMostImprovedCard2,
                    binding.tagMostImproved2Name,
                    binding.tagMostImproved2Stats
                ) { t ->
                    val prev = (t.avgPreviousCompletionRate * 100).toInt()
                    val curr = (t.avgCompletionRate * 100).toInt()
                    "${t.personaCount} personas, completion: $prev% → $curr%"
                }

                binding.tagMostImprovedLabel.visibility = if (report.tagsMostImproved.isEmpty()) View.GONE else View.VISIBLE

                // Tags Need Attention
                bindTagCard(
                    report.tagsNeedAttention.getOrNull(0),
                    binding.tagNeedAttentionCard1,
                    binding.tagNeedAttention1Name,
                    binding.tagNeedAttention1Stats
                ) { t ->
                    val completion = (t.avgCompletionRate * 100).toInt()
                    if (t.avgOpenCount < 1.0) "${t.personaCount} personas, not opened"
                    else "${t.personaCount} personas, avg completion: $completion%"
                }

                bindTagCard(
                    report.tagsNeedAttention.getOrNull(1),
                    binding.tagNeedAttentionCard2,
                    binding.tagNeedAttention2Name,
                    binding.tagNeedAttention2Stats
                ) { t ->
                    val completion = (t.avgCompletionRate * 100).toInt()
                    if (t.avgOpenCount < 1.0) "${t.personaCount} personas, not opened"
                    else "${t.personaCount} personas, avg completion: $completion%"
                }

                binding.tagNeedAttentionLabel.visibility = if (report.tagsNeedAttention.isEmpty()) View.GONE else View.VISIBLE

                // Update list
                adapter.submitList(report.personaReports)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.mostActiveLabel.visibility = View.GONE
                binding.mostActiveCard1.visibility = View.GONE
                binding.mostActiveCard2.visibility = View.GONE
                binding.mostImprovedLabel.visibility = View.GONE
                binding.mostImprovedCard1.visibility = View.GONE
                binding.mostImprovedCard2.visibility = View.GONE
                binding.needsAttentionLabel.visibility = View.GONE
                binding.needsAttentionCard1.visibility = View.GONE
                binding.needsAttentionCard2.visibility = View.GONE
                binding.tagInsightsLabel.visibility = View.GONE
                binding.tagMostActiveLabel.visibility = View.GONE
                binding.tagMostActiveCard1.visibility = View.GONE
                binding.tagMostActiveCard2.visibility = View.GONE
                binding.tagMostImprovedLabel.visibility = View.GONE
                binding.tagMostImprovedCard1.visibility = View.GONE
                binding.tagMostImprovedCard2.visibility = View.GONE
                binding.tagNeedAttentionLabel.visibility = View.GONE
                binding.tagNeedAttentionCard1.visibility = View.GONE
                binding.tagNeedAttentionCard2.visibility = View.GONE
                adapter.submitList(emptyList())
            }
        }
    }

    private fun bindHighlightCard(
        report: PersonaReport?,
        card: View,
        nameView: android.widget.TextView,
        statsView: android.widget.TextView,
        tagsChipGroup: com.google.android.material.chip.ChipGroup,
        statsFormatter: (PersonaReport) -> String
    ) {
        if (report == null) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE
        nameView.text = report.persona.name
        statsView.text = statsFormatter(report)
        PersonaReportAdapter.populateTags(tagsChipGroup, report.tags)
    }

    private fun bindTagCard(
        tagReport: TagReport?,
        card: View,
        nameView: android.widget.TextView,
        statsView: android.widget.TextView,
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

    private fun getNeedsAttentionMessage(report: PersonaReport): String {
        return when {
            report.currentOpenCount == 0 -> {
                val days = when (currentReportType) {
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
}
