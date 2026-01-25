package com.samsara.polymath

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.samsara.polymath.adapter.PersonaReportAdapter
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

                // Update highlights
                if (report.mostActive != null) {
                    binding.mostActiveCard.visibility = View.VISIBLE
                    binding.mostActivePersonaName.text = report.mostActive.persona.name
                    binding.mostActivePersonaStats.text = "Opened ${report.mostActive.currentOpenCount} times"
                } else {
                    binding.mostActiveCard.visibility = View.GONE
                }

                if (report.mostImproved != null && report.mostImproved.improvementScore > 0) {
                    binding.mostImprovedCard.visibility = View.VISIBLE
                    binding.mostImprovedPersonaName.text = report.mostImproved.persona.name

                    val currentRate = (report.mostImproved.currentCompletionRate * 100).toInt()
                    val previousRate = (report.mostImproved.previousCompletionRate * 100).toInt()
                    val change = currentRate - previousRate

                    binding.mostImprovedPersonaStats.text =
                        "Completion rate: $previousRate% → $currentRate% (+$change%)"
                } else {
                    binding.mostImprovedCard.visibility = View.GONE
                }

                if (report.needsAttention != null) {
                    binding.needsAttentionCard.visibility = View.VISIBLE
                    binding.needsAttentionPersonaName.text = report.needsAttention.persona.name

                    val message = when {
                        report.needsAttention.currentOpenCount == 0 -> {
                            val days = when (currentReportType) {
                                ReportType.WEEKLY -> "7 days"
                                ReportType.MONTHLY -> "30 days"
                            }
                            "Not opened in $days"
                        }
                        report.needsAttention.currentCompletionRate < 0.3 -> {
                            "Low completion rate: ${(report.needsAttention.currentCompletionRate * 100).toInt()}%"
                        }
                        else -> "Needs more attention"
                    }
                    binding.needsAttentionPersonaStats.text = message
                } else {
                    binding.needsAttentionCard.visibility = View.GONE
                }

                // Update list
                adapter.submitList(report.personaReports)
            } catch (e: Exception) {
                e.printStackTrace()
                // Hide all cards on error
                binding.mostActiveCard.visibility = View.GONE
                binding.mostImprovedCard.visibility = View.GONE
                binding.needsAttentionCard.visibility = View.GONE
                adapter.submitList(emptyList())
            }
        }
    }
}

