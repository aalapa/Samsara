package com.samsara.polymath.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.samsara.polymath.data.DailyPersonaTimeSumWithDay
import com.samsara.polymath.data.PersonaStatistics
import com.samsara.polymath.data.PersonaReport
import com.samsara.polymath.databinding.FragmentReportChartsBinding
import com.samsara.polymath.viewmodel.PersonaReportViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReportChartsFragment : Fragment() {

    private var _binding: FragmentReportChartsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonaReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe stacked bar chart data
        viewModel.dailyTimeByPersona.observe(viewLifecycleOwner) { dailyData ->
            viewModel.personaInfo.value?.let { personaInfo ->
                setupStackedBarChart(binding.stackedBarChart, dailyData, personaInfo)
            }
        }

        // Observe line chart data
        viewModel.personaScoreHistory.observe(viewLifecycleOwner) { scoreHistory ->
            viewModel.personaInfo.value?.let { personaInfo ->
                setupLineChart(binding.lineChart, scoreHistory, personaInfo)
            }
        }

        // Observe bar + pie chart data from report summary
        viewModel.reportSummary.observe(viewLifecycleOwner) { report ->
            val personaReports = report.personaReports
            setupHorizontalBarChart(binding.horizontalBarChart, personaReports)
            setupPieChart(binding.pieChart, personaReports)
        }

        // Also re-render stacked bar and line chart when personaInfo arrives
        viewModel.personaInfo.observe(viewLifecycleOwner) { personaInfo ->
            viewModel.dailyTimeByPersona.value?.let { dailyData ->
                setupStackedBarChart(binding.stackedBarChart, dailyData, personaInfo)
            }
            viewModel.personaScoreHistory.value?.let { scoreHistory ->
                setupLineChart(binding.lineChart, scoreHistory, personaInfo)
            }
        }
    }

    // ==================== STACKED BAR CHART ====================

    private fun setupStackedBarChart(
        chart: BarChart,
        dailyData: List<DailyPersonaTimeSumWithDay>,
        personaInfo: Map<Long, Pair<String, String>>
    ) {
        if (dailyData.isEmpty()) {
            chart.setNoDataText(getString(com.samsara.polymath.R.string.chart_no_data))
            chart.invalidate()
            return
        }

        // Group daily data into weekly buckets
        val now = System.currentTimeMillis()
        val weekMillis = TimeUnit.DAYS.toMillis(7)

        // Collect all persona IDs in stable order
        val personaIds = dailyData.map { it.personaId }.distinct()

        // Group by week number (0 = oldest week, 12 = most recent)
        val sinceMillis = now - TimeUnit.DAYS.toMillis(91)
        val weeklyData = mutableMapOf<Int, MutableMap<Long, Float>>() // weekIndex -> (personaId -> hours)

        for (entry in dailyData) {
            val weekIndex = ((entry.dayMillis - sinceMillis) / weekMillis).toInt().coerceIn(0, 12)
            val personaMap = weeklyData.getOrPut(weekIndex) { mutableMapOf() }
            val currentHours = personaMap.getOrDefault(entry.personaId, 0f)
            personaMap[entry.personaId] = currentHours + (entry.totalTime / 3600000f)
        }

        // Build stacked bar entries
        val barEntries = mutableListOf<BarEntry>()
        val weekLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        for (weekIdx in 0..12) {
            val weekStart = sinceMillis + weekIdx * weekMillis
            weekLabels.add(dateFormat.format(Date(weekStart)))

            val values = FloatArray(personaIds.size)
            val personaMap = weeklyData[weekIdx] ?: emptyMap()
            for ((i, pid) in personaIds.withIndex()) {
                values[i] = personaMap[pid] ?: 0f
            }
            barEntries.add(BarEntry(weekIdx.toFloat(), values))
        }

        val dataSet = BarDataSet(barEntries, "").apply {
            // Set colors for each stack segment
            colors = personaIds.map { pid ->
                val colorStr = personaInfo[pid]?.second ?: "#007AFF"
                try { Color.parseColor(colorStr) } catch (_: Exception) { Color.parseColor("#007AFF") }
            }
            // Set stack labels
            stackLabels = personaIds.map { pid ->
                personaInfo[pid]?.first ?: "Unknown"
            }.toTypedArray()
            setDrawValues(false)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.7f
        }

        chart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(weekLabels)
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
                textSize = 9f
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                textSize = 10f
            }

            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 12f
                yOffset = 10f
                isWordWrapEnabled = true
            }

            setExtraBottomOffset(12f)
            animateY(600)
            invalidate()
        }
    }

    // ==================== LINE CHART ====================

    private fun setupLineChart(
        chart: LineChart,
        scoreHistory: Map<Long, List<PersonaStatistics>>,
        personaInfo: Map<Long, Pair<String, String>>
    ) {
        if (scoreHistory.isEmpty() || scoreHistory.values.all { it.isEmpty() }) {
            chart.setNoDataText(getString(com.samsara.polymath.R.string.chart_no_data))
            chart.invalidate()
            return
        }

        val dataSets = mutableListOf<LineDataSet>()
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val allTimestamps = mutableSetOf<Long>()

        for ((personaId, stats) in scoreHistory) {
            if (stats.size < 2) continue // Need at least 2 points for a line

            val info = personaInfo[personaId]
            val name = info?.first ?: "Unknown"
            val colorStr = info?.second ?: "#007AFF"
            val lineColor = try { Color.parseColor(colorStr) } catch (_: Exception) { Color.parseColor("#007AFF") }

            val entries = stats.sortedBy { it.timestamp }.map { stat ->
                allTimestamps.add(stat.timestamp)
                Entry(stat.timestamp.toFloat(), stat.score.toFloat())
            }

            val lineDataSet = LineDataSet(entries, name).apply {
                color = lineColor
                setCircleColor(lineColor)
                circleRadius = 3f
                lineWidth = 2f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(false)
            }
            dataSets.add(lineDataSet)
        }

        if (dataSets.isEmpty()) {
            chart.setNoDataText(getString(com.samsara.polymath.R.string.chart_no_data))
            chart.invalidate()
            return
        }

        val lineData = LineData(dataSets.toList())

        chart.apply {
            data = lineData
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = TimeUnit.DAYS.toMillis(7).toFloat()
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
                labelRotationAngle = -45f
                textSize = 9f
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                textSize = 10f
            }

            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 12f
                yOffset = 10f
                isWordWrapEnabled = true
            }

            setExtraBottomOffset(12f)
            animateX(600)
            invalidate()
        }
    }

    // ==================== HORIZONTAL BAR CHART ====================

    private fun setupHorizontalBarChart(
        chart: HorizontalBarChart,
        personaReports: List<PersonaReport>
    ) {
        val reportsWithTime = personaReports.filter { it.totalTimeSpent > 0 }
            .sortedBy { it.totalTimeSpent }

        if (reportsWithTime.isEmpty()) {
            chart.setNoDataText(getString(com.samsara.polymath.R.string.chart_no_data))
            chart.invalidate()
            return
        }

        val entries = reportsWithTime.mapIndexed { index, report ->
            BarEntry(index.toFloat(), report.totalTimeSpent / 3600000f) // Convert to hours
        }

        val colors = reportsWithTime.map { report ->
            try { Color.parseColor(report.persona.backgroundColor) } catch (_: Exception) { Color.parseColor("#007AFF") }
        }

        val labels = reportsWithTime.map { it.persona.name }

        val dataSet = BarDataSet(entries, "").apply {
            this.colors = colors
            setDrawValues(true)
            valueTextSize = 10f
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value >= 1f) String.format("%.1fh", value)
                    else String.format("%.0fm", value * 60)
                }
            }
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }

        // Adjust chart height based on number of personas
        val minHeight = (reportsWithTime.size * 48).coerceAtLeast(150)
        chart.layoutParams = chart.layoutParams.apply {
            height = (minHeight * resources.displayMetrics.density).toInt()
        }

        chart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                setDrawGridLines(false)
                textSize = 11f
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                textSize = 10f
            }

            axisRight.isEnabled = false
            legend.isEnabled = false

            setExtraLeftOffset(8f)
            animateX(600)
            invalidate()
        }
    }

    // ==================== PIE CHART ====================

    private fun setupPieChart(
        chart: PieChart,
        personaReports: List<PersonaReport>
    ) {
        val reportsWithTime = personaReports.filter { it.totalTimeSpent > 0 }
            .sortedByDescending { it.totalTimeSpent }

        if (reportsWithTime.isEmpty()) {
            chart.setNoDataText(getString(com.samsara.polymath.R.string.chart_no_data))
            chart.invalidate()
            return
        }

        val entries = reportsWithTime.map { report ->
            PieEntry(report.totalTimeSpent.toFloat(), report.persona.name)
        }

        val colors = reportsWithTime.map { report ->
            try { Color.parseColor(report.persona.backgroundColor) } catch (_: Exception) { Color.parseColor("#007AFF") }
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 2f
            selectionShift = 5f
            valueTextSize = 11f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(chart)
        }

        val pieData = PieData(dataSet)

        chart.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 45f
            transparentCircleRadius = 50f
            setHoleColor(Color.WHITE)
            setUsePercentValues(true)
            setEntryLabelColor(Color.DKGRAY)
            setEntryLabelTextSize(10f)

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 12f
                yOffset = 10f
                isWordWrapEnabled = true
            }

            setExtraBottomOffset(8f)
            animateY(600)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
