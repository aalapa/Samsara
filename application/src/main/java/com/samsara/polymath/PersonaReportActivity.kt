package com.samsara.polymath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.samsara.polymath.adapter.ReportPagerAdapter
import com.samsara.polymath.data.ReportType
import com.samsara.polymath.databinding.ActivityPersonaReportBinding
import com.samsara.polymath.viewmodel.PersonaReportViewModel
import java.text.SimpleDateFormat
import java.util.*

class PersonaReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonaReportBinding
    private lateinit var viewModel: PersonaReportViewModel

    private val tabTitles by lazy {
        arrayOf(
            getString(R.string.report_tab_overview),
            getString(R.string.report_tab_time),
            getString(R.string.report_tab_trends)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonaReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel = ViewModelProvider(this)[PersonaReportViewModel::class.java]

        // Setup ViewPager2 + TabLayout
        val pagerAdapter = ReportPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Period selector buttons
        binding.weeklyButton.setOnClickListener {
            viewModel.setReportType(ReportType.WEEKLY)
            updateButtonStates()
        }

        binding.monthlyButton.setOnClickListener {
            viewModel.setReportType(ReportType.MONTHLY)
            updateButtonStates()
        }

        // Observe date range from report
        viewModel.reportSummary.observe(this) { report ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.dateRangeTextView.text = "${dateFormat.format(Date(report.startDate))} - ${dateFormat.format(Date(report.endDate))}"
        }

        updateButtonStates()

        // Initial load + save statistics
        viewModel.loadReport()
        viewModel.saveCurrentStatistics()
    }

    private fun updateButtonStates() {
        when (viewModel.getCurrentReportType()) {
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
}
