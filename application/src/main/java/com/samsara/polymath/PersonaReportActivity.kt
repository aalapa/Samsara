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
