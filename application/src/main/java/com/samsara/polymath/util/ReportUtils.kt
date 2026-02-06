package com.samsara.polymath.util

import android.view.View
import android.widget.ImageView

object ReportUtils {

    /**
     * Wire an accordion header to toggle visibility of its content section.
     * Arrow rotates 180° on expand, back to 0° on collapse.
     */
    fun setupAccordion(header: View, content: View, arrow: ImageView) {
        header.setOnClickListener {
            val isVisible = content.visibility == View.VISIBLE
            content.visibility = if (isVisible) View.GONE else View.VISIBLE
            arrow.animate().rotation(if (isVisible) 0f else 180f).setDuration(200).start()
        }
    }

    /**
     * Format milliseconds to a human-readable duration string (e.g. "2h 15m" or "45m").
     */
    fun formatDuration(millis: Long): String {
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
