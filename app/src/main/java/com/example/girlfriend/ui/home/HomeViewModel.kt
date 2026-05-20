package com.example.girlfriend.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.girlfriend.data.AppDatabase
import com.example.girlfriend.data.entity.Anniversary
import com.example.girlfriend.util.CalendarHelper
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val anniversaryDao = db.anniversaryDao()

    val anniversaries: LiveData<List<Anniversary>> = anniversaryDao.getAll()

    fun save(anniversary: Anniversary) {
        viewModelScope.launch {
            val existing = if (anniversary.id.isNotEmpty()) {
                anniversaryDao.getById(anniversary.id)
            } else null

            val calId = CalendarHelper.getOrCreateCalendarId(getApplication())

            if (existing != null) {
                // 删除旧的日历事件
                CalendarHelper.deleteEvents(getApplication(), existing.calendarEventIds)

                // 创建新的 3 个日历事件
                val newIds = CalendarHelper.insertEvents(getApplication(), anniversary, calId)
                val updated = anniversary.copy(
                    calendarEventIds = newIds.joinToString(",")
                )
                anniversaryDao.update(updated)
            } else {
                // 新增
                val newIds = CalendarHelper.insertEvents(getApplication(), anniversary, calId)
                val toSave = anniversary.copy(
                    calendarEventIds = newIds.joinToString(",")
                )
                anniversaryDao.insert(toSave)
            }
        }
    }

    fun delete(anniversary: Anniversary) {
        viewModelScope.launch {
            CalendarHelper.deleteEvents(getApplication(), anniversary.calendarEventIds)
            anniversaryDao.delete(anniversary)
        }
    }
}
