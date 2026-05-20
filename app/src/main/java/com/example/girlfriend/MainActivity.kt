package com.example.girlfriend

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.girlfriend.ui.dislikes.DislikesFragment
import com.example.girlfriend.ui.gifts.GiftsFragment
import com.example.girlfriend.ui.home.HomeFragment
import com.example.girlfriend.ui.likes.LikesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CALENDAR = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestCalendarPermissions()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // 默认首页
        if (savedInstanceState == null) {
            switchToFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchToFragment(HomeFragment())
                R.id.nav_likes -> switchToFragment(LikesFragment())
                R.id.nav_dislikes -> switchToFragment(DislikesFragment())
                R.id.nav_gifts -> switchToFragment(GiftsFragment())
            }
            true
        }
    }

    fun switchToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun requestCalendarPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                ),
                PERMISSION_REQUEST_CALENDAR
            )
        }
    }
}
