package com.example.firesense_app

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Mission
import com.example.firesenseapp.Utils

@Suppress("DEPRECATION")
internal class MyAdapter(
        var context: Context,
        fm: FragmentManager,
        var totalTabs: Int,
        var mission: Mission,
        var imagesMosaic: ArrayList<String>,
        var imagesRgb: ArrayList<String>,
        var imagesThermal: ArrayList<String>,
        var fire: String,
        var utils: Utils,
        var comm: Communication,
        var connect: Boolean
) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MissionInformation(mission)
            }
            1 -> {
                MissionMosaic(mission, imagesMosaic, fire, utils, comm, connect)
            }
            2 -> {
                MissionRgb(mission, imagesRgb, fire, utils, comm, connect)
            }
            3 -> {
                MissionThermal(mission, imagesThermal, fire, utils, comm, connect)
            }
            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}
