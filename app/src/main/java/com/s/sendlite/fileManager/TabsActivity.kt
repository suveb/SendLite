package com.s.sendlite.fileManager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.s.sendlite.R
import com.s.sendlite.Utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_tabs.*

class TabsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        view_pager.adapter =
            ViewPagerAdapter(this as FragmentActivity)

        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Files"
                1 -> "Videos"
                2 -> "Apps"
                3 -> "Photos"
                else -> "Music"
            }
        }.attach()
    }
}