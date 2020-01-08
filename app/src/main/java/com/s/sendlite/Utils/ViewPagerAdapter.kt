package com.s.sendlite.Utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.s.sendlite.fileManager.AppsFragment.AppsFragment
import com.s.sendlite.fileManager.FilesFragment.FilesFragment
import com.s.sendlite.fileManager.MusicFragment.MusicFragment
import com.s.sendlite.fileManager.PhotosFragment.PhotosFragment
import com.s.sendlite.fileManager.VideosFragment.VideosFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FilesFragment()
            1 -> VideosFragment()
            2 -> AppsFragment()
            3 -> PhotosFragment()
            else -> MusicFragment()
        }
    }
}