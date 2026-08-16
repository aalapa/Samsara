package com.samsara.polymath.fragment

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.samsara.polymath.data.PersonaStatistics
import com.samsara.polymath.databinding.FragmentReportChartsBinding
import com.samsara.polymath.viewmodel.PersonaReportViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReportChartsFragment : Fragment() {

    private var _binding: FragmentReportChartsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonaReportViewModel by activityViewModels()

    private val isDarkMode: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private val chartTextColor: Int
        get() = if (isDarkMode) Color.WHITE else Color.DKGRAY

    private val chartGridColor: Int
        get() = if (isDarkMode) Color.parseColor("#555555") else Color.parseColor("#E0E0E0")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.personaScoreHistory.observe(viewLifecycleOwner) { scoreHistory ->
            viewModel.personaInfo.value?.let { personaInfo ->
                setupLineChart(binding.lineChart, scoreHistory, personaInfo)
            }
        }

        viewModel.personaInfo.observe(viewLifecycleOwner) { personaInfo ->
            viewModel.personaScoreHistory.value?.let { scoreHistory ->
                setupLineChart(binding.lineChart, scoreHistory, personaInfo)
            }
        }
    }

    private fun setupLineChart(
        chart: LineChart,
        scoreHistory: Map<Long, List<PersonaStatistics>>,
        personaInfo: Map<Long, Pair<String, String>>
    ) {
        if (scoreHistory.isEmpty() || scoreHistory.values.all { it.isEmpty() }) {
            chart.setNoDataText(getString(com.samsara.polymath.R.string.chart_no_data))
            chart.setNoDataTextColor(chartTextColor)
            chart.invalidate()
            return
        }

        val now = System.currentTimeMillis()
        val dayMillis = TimeUnit.DAYS.toMillis(1)

        val dataSets = mutableListOf<LineDataSet>()

        for ((personaId, stats) in scoreHistory) {
            if (stats.isEmpty()) continue

            val info = personaInfo[personaId]
            val name = info?.first ?: "Unknown"
            val colorStr = info?.second ?: "#007AFF"
            val lineColor = try { Color.parseColor(colorStr) } catch (_: Exception) { Color.parseColor("#007AFF") }

            val entries = stats.map { stat ->
                val daysAgo = ((now - stat.timestamp) / dayMillis).toFloat().coerceIn(0f, 91f)
                val logScore = Math.log1p(stat.score).toFloat()
                Entry(daysAgo, logScore)
            }.sortedBy { it.x }

            val lineDataSet = LineDataSet(entries, name).apply {
                color = lineColor
                setCircleColor(lineColor)
                circleRadius = if (entries.size == 1) 5f else 3f
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(true)
                if (entries.size > 1) {
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                setDrawFilled(false)
            }
            dataSets.add(lineDataSet)
        }

        if (dataSets.isEmpty()) {
            chart.setNoDataText(getString(com.samsara.polymath.R.string.chart_no_data))
            chart.setNoDataTextColor(chartTextColor)
            chart.invalidate()
            return
        }

        val lineData = LineData(dataSets.toList())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        chart.apply {
            data = lineData
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 7f
                axisMinimum = 0f
                axisMaximum = 91f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val millis = now - (value.toLong() * dayMillis)
                        return dateFormat.format(Date(millis))
                    }
                }
                labelRotationAngle = -45f
                textSize = 9f
                textColor = chartTextColor
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = chartGridColor
                textSize = 10f
                textColor = chartTextColor
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val actual = Math.expm1(value.toDouble())
                        return if (actual >= 10) String.format("%.0f", actual)
                        else String.format("%.1f", actual)
                    }
                }
            }

            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(true)
                xEntrySpace = 6f
                yEntrySpace = 4f
                textSize = 10f
                textColor = chartTextColor
            }

            setExtraBottomOffset(4f)
            setNoDataTextColor(chartTextColor)
            animateX(600)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
