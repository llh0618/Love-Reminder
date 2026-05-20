package com.example.girlfriend

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
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
        private const val PREFS_NAME = "girlfriend_app"
        private const val KEY_FIRST_LAUNCH_DONE = "first_launch_done_v1"
        private const val CALENDAR_ACCOUNT = "girlfriend_memo@local"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestCalendarPermissions()
        handleFirstLaunch()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

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

    /**
     * 首次安装（卸载后重装）：清理旧日历数据
     * 更新安装：SharedPreferences 保留，不清理
     */
    private fun handleFirstLaunch() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_FIRST_LAUNCH_DONE, false)) return

        // 清理旧日历（删除整个日历及其所有事件）
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ?"
        val args = arrayOf(CALENDAR_ACCOUNT)

        contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI, projection, selection, args, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val calId = cursor.getLong(0)
                val uri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calId)
                contentResolver.delete(uri, null, null)
            }
        }

        prefs.edit().putBoolean(KEY_FIRST_LAUNCH_DONE, true).apply()
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

