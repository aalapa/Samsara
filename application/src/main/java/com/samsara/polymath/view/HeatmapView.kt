package com.samsara.polymath.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Calendar

/**
 * GitHub-style heatmap showing daily tracked time over the last 90 days.
 * 7 rows (Mon-Sun) x 13 columns (weeks). Each cell colored by time intensity.
 * Tap a cell to trigger the onDayClickListener callback with the day's millis.
 */
class HeatmapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Map of day-start-millis -> total tracked time in millis
    private var dayData: Map<Long, Long> = emptyMap()
    private var maxTime: Long = 0L

    // Selected day (highlighted)
    private var selectedDay: Long? = null

    var onDayClickListener: ((dayMillis: Long) -> Unit)? = null

    // Grid dimensions
    private val numRows = 7   // Mon..Sun
    private val numCols = 13  // 13 weeks

    // Colors: 5 intensity levels (empty → ink at increasing opacity)
    private val emptyColor = Color.parseColor("#F0EFEA")
    private val level1Color = Color.argb(51, 22, 23, 26)
    private val level2Color = Color.argb(102, 22, 23, 26)
    private val level3Color = Color.argb(163, 22, 23, 26)
    private val level4Color = Color.argb(217, 22, 23, 26)
    private val selectedStrokeColor = Color.parseColor("#16171A")

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val selectedStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = selectedStrokeColor
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#85878C")
        textSize = 24f
        textAlign = Paint.Align.RIGHT
    }

    private val monthLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#85878C")
        textSize = 22f
        textAlign = Paint.Align.LEFT
    }

    // Each cell maps to a calendar day
    private var cellDays: Array<LongArray> = Array(numCols) { LongArray(numRows) }

    // Layout metrics
    private var cellSize = 0f
    private var cellGap = 0f
    private var labelWidth = 0f
    private var monthLabelHeight = 0f
    private var gridLeft = 0f
    private var gridTop = 0f

    private val dayLabels = arrayOf("M", "", "W", "", "F", "", "S")
    private val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun setData(data: Map<Long, Long>) {
        dayData = data
        maxTime = data.values.maxOrNull() ?: 0L
        computeCellDays()
        invalidate()
    }

    fun setSelectedDay(dayMillis: Long?) {
        selectedDay = dayMillis
        invalidate()
    }

    private fun computeCellDays() {
        val cal = Calendar.getInstance()
        // End at today
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val today = cal.timeInMillis
        val todayDow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon, ..., 7=Sat

        // Convert to 0=Mon..6=Sun
        val todayRow = when (todayDow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        // The last column ends at today's row. Fill backwards.
        cellDays = Array(numCols) { LongArray(numRows) { -1L } }

        // Start from the last cell (today) and go backwards
        var currentDay = today
        for (col in numCols - 1 downTo 0) {
            val endRow = if (col == numCols - 1) todayRow else 6
            for (row in endRow downTo 0) {
                cellDays[col][row] = currentDay
                currentDay -= 86400000L // go back one day
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density

        labelWidth = 24f * density
        monthLabelHeight = 20f * density
        cellGap = 3f * density

        val availableWidth = w - paddingLeft - paddingRight - labelWidth
        cellSize = ((availableWidth - cellGap * (numCols - 1)) / numCols).coerceAtMost(9f * density)

        val totalHeight = monthLabelHeight + numRows * cellSize + (numRows - 1) * cellGap + paddingTop + paddingBottom
        setMeasuredDimension(w, totalHeight.toInt())

        gridLeft = paddingLeft + labelWidth
        gridTop = paddingTop + monthLabelHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cellDays.isEmpty()) {
            computeCellDays()
        }

        val cornerRadius = 2.5f * resources.displayMetrics.density
        val rect = RectF()
        val cal = Calendar.getInstance()

        // Draw day labels (M, W, F, S)
        for (row in 0 until numRows) {
            val label = dayLabels[row]
            if (label.isNotEmpty()) {
                val y = gridTop + row * (cellSize + cellGap) + cellSize / 2f + labelPaint.textSize / 3f
                canvas.drawText(label, gridLeft - 6f * resources.displayMetrics.density, y, labelPaint)
            }
        }

        // Draw month labels above columns
        var lastMonth = -1
        for (col in 0 until numCols) {
            val dayMillis = cellDays[col][0]
            if (dayMillis <= 0) continue
            cal.timeInMillis = dayMillis
            val month = cal.get(Calendar.MONTH)
            if (month != lastMonth) {
                lastMonth = month
                val x = gridLeft + col * (cellSize + cellGap)
                canvas.drawText(monthNames[month], x, gridTop - 4f * resources.displayMetrics.density, monthLabelPaint)
            }
        }

        // Draw cells
        for (col in 0 until numCols) {
            for (row in 0 until numRows) {
                val dayMillis = cellDays[col][row]
                if (dayMillis < 0) continue

                val left = gridLeft + col * (cellSize + cellGap)
                val top = gridTop + row * (cellSize + cellGap)
                rect.set(left, top, left + cellSize, top + cellSize)

                val timeForDay = dayData[normalizeDay(dayMillis)] ?: 0L
                cellPaint.color = getColorForTime(timeForDay)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint)

                // Draw selection highlight
                if (selectedDay != null && normalizeDay(dayMillis) == normalizeDay(selectedDay!!)) {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, selectedStrokePaint)
                }
            }
        }
    }

    private fun normalizeDay(millis: Long): Long {
        return (millis / 86400000L) * 86400000L
    }

    private fun getColorForTime(timeMillis: Long): Int {
        if (timeMillis <= 0 || maxTime <= 0) return emptyColor
        val ratio = timeMillis.toFloat() / maxTime
        return when {
            ratio <= 0.0f -> emptyColor
            ratio <= 0.25f -> level1Color
            ratio <= 0.50f -> level2Color
            ratio <= 0.75f -> level3Color
            else -> level4Color
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y

            // Determine which cell was tapped
            for (col in 0 until numCols) {
                for (row in 0 until numRows) {
                    val dayMillis = cellDays[col][row]
                    if (dayMillis < 0) continue

                    val left = gridLeft + col * (cellSize + cellGap)
                    val top = gridTop + row * (cellSize + cellGap)
                    if (x >= left && x <= left + cellSize && y >= top && y <= top + cellSize) {
                        val normalized = normalizeDay(dayMillis)
                        val timeForDay = dayData[normalized] ?: 0L
                        if (timeForDay > 0) {
                            selectedDay = normalized
                            invalidate()
                            onDayClickListener?.invoke(normalized)
                        }
                        return true
                    }
                }
            }
        }
        return true
    }
}
