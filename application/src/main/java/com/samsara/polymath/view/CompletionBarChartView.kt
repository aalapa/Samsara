package com.samsara.polymath.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CompletionBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var intervals: List<Int> = emptyList()
    private var barColor: Int = Color.parseColor("#007AFF")

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val axisLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    fun setIntervals(data: List<Int>, color: Int = Color.parseColor("#007AFF")) {
        intervals = data
        barColor = color
        barPaint.color = color
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = if (intervals.isEmpty()) 0 else 200.dp
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (intervals.isEmpty()) return

        val maxVal = intervals.max().coerceAtLeast(1)
        val barCount = intervals.size
        val paddingH = 16f.dpF
        val paddingTop = 32f
        val paddingBottom = 28f
        val chartWidth = width - paddingH * 2
        val chartHeight = height - paddingTop - paddingBottom
        val barSpacing = 8f.dpF
        val barWidth = ((chartWidth - barSpacing * (barCount - 1)) / barCount).coerceAtMost(48f.dpF)
        val totalBarsWidth = barWidth * barCount + barSpacing * (barCount - 1)
        val startX = (width - totalBarsWidth) / 2f

        val rect = RectF()
        val cornerRadius = 6f.dpF

        intervals.forEachIndexed { index, value ->
            val barHeight = (value.toFloat() / maxVal) * chartHeight
            val left = startX + index * (barWidth + barSpacing)
            val top = paddingTop + (chartHeight - barHeight)
            val right = left + barWidth
            val bottom = paddingTop + chartHeight

            rect.set(left, top, right, bottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)

            // Day count label on top
            canvas.drawText(
                value.toString(),
                left + barWidth / 2,
                top - 6f,
                labelPaint
            )

            // Index label below
            canvas.drawText(
                "#${index + 1}",
                left + barWidth / 2,
                bottom + 20f,
                axisLabelPaint
            )
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dpF: Float get() = this * resources.displayMetrics.density
}
