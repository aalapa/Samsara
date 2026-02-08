package com.samsara.polymath.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.samsara.polymath.fragment.ReportChartsFragment
import com.samsara.polymath.fragment.ReportOverviewFragment
import com.samsara.polymath.fragment.ReportTimeFragment
import com.samsara.polymath.fragment.ReportTrendsFragment

class ReportPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReportOverviewFragment()
            1 -> ReportTimeFragment()
            2 -> ReportTrendsFragment()
            3 -> ReportChartsFragment()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }
}
