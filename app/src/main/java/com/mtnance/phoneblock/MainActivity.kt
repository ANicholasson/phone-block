package com.mtnance.phoneblock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mtnance.phoneblock.fragments.HomeFragment
import com.mtnance.phoneblock.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Building the BottomAppBar (with floating action button (cradle))
        bottom_navigation_view.background = null
        bottom_navigation_view.menu.getItem(1).isEnabled = false;

        // Initialize fragments (part of the "tab-bar" navigation)
        val homeFragment = HomeFragment()
        val settingsFragment = SettingsFragment()

        makeCurrentFragment(homeFragment)

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> makeCurrentFragment(homeFragment)
                R.id.settings -> makeCurrentFragment(settingsFragment)
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            commit()
        }
    }
}