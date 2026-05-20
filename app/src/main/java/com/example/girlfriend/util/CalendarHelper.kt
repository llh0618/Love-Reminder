package com.example.girlfriend.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.example.girlfriend.data.entity.Anniversary
import java.util.Calendar
import java.util.TimeZone

object CalendarHelper {

    private const val CALENDAR_NAME = "女友助手"
    private const val ACCOUNT_NAME = "girlfriend_memo@local"

    fun getOrCreateCalendarId(context: Context): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ?"
        val args = arrayOf(ACCOUNT_NAME)

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection, selection, args, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }

        val values = ContentValues().apply {
            put(CalendarContract.Calendars.NAME, CALENDAR_NAME)
            put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF6B81.toInt())
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
        }

        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()

        val newUri = context.contentResolver.insert(uri, values)
        return newUri?.lastPathSegment?.toLongOrNull() ?: -1L
    }

    /**
     * 为纪念日创建 3 个日历事件：当天、提前3天、提前7天
     * 返回所有 eventId 的列表
     */
    fun insertEvents(context: Context, anniversary: Anniversary, calendarId: Long): List<Long> {
        val dateParts = anniversary.date.split("-").map { it.toInt() }
        val emoji = getTypeEmoji(anniversary.type)
        val rrule = if (anniversary.repeat == "yearly") "FREQ=YEARLY" else null
        val eventIds = mutableListOf<Long>()
        val days = anniversary.remindBeforeDays

        // 当天事件始终创建
        eventIds.add(
            insertOneEvent(context, calendarId, anniversary, dateParts, 0,
                "$emoji ${anniversary.name}", rrule)
        )

        // 提前 3 天
        if (days >= 3) {
            eventIds.add(
                insertOneEvent(context, calendarId, anniversary, dateParts, -3,
                    "🔔 ${anniversary.name} · 还有3天", rrule)
            )
        }

        // 提前 7 天
        if (days >= 7) {
            eventIds.add(
                insertOneEvent(context, calendarId, anniversary, dateParts, -7,
                    "🔔 ${anniversary.name} · 还有7天", rrule)
            )
        }

        return eventIds.filter { it > 0 }
    }

    private fun insertOneEvent(
        context: Context,
        calendarId: Long,
        anniversary: Anniversary,
        dateParts: List<Int>,
        dayOffset: Int,
        title: String,
        rrule: String?
    ): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateParts[0])
            set(Calendar.MONTH, dateParts[1] - 1)
            set(Calendar.DAY_OF_MONTH, dateParts[2])
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (dayOffset != 0) {
            cal.add(Calendar.DAY_OF_MONTH, dayOffset)
        }

        val beginTime = cal.timeInMillis
        val endTime = beginTime + 3600_000

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, buildDescription(anniversary, dayOffset))
            put(CalendarContract.Events.DTSTART, beginTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.ALL_DAY, 1)
            put(CalendarContract.Events.HAS_ALARM, 0)

            if (rrule != null) {
                put(CalendarContract.Events.RRULE, rrule)
            }
        }

        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return uri?.lastPathSegment?.toLongOrNull() ?: -1L
    }

    /**
     * 批量删除日历事件
     */
    fun deleteEvents(context: Context, eventIds: String) {
        if (eventIds.isBlank()) return
        eventIds.split(",").forEach { idStr ->
            val id = idStr.trim().toLongOrNull() ?: return@forEach
            if (id > 0) {
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
                context.contentResolver.delete(uri, null, null)
            }
        }
    }

    private fun getTypeEmoji(type: String): String = when (type) {
        "valentine" -> "💝"
        "520" -> "💕"
        "qixi" -> "💗"
        "birthday" -> "🎂"
        "anniversary" -> "💍"
        else -> "💖"
    }

    private fun buildDescription(a: Anniversary, offset: Int): String = buildString {
        append("类型：${typeLabel(a.type)}\n")
        append("重复：${if (a.repeat == "yearly") "每年" else "仅此一次"}\n")
        when (offset) {
            0 -> append("📍 纪念日当天\n")
            -3 -> append("📍 提前3天提醒\n")
            -7 -> append("📍 提前7天提醒\n")
        }
        if (a.note.isNotBlank()) append("备注：${a.note}\n")
        append("\n— 女友助手")
    }

    fun typeLabel(type: String): String = when (type) {
        "valentine" -> "情人节"
        "520" -> "520"
        "qixi" -> "七夕"
        "birthday" -> "生日"
        "anniversary" -> "纪念日"
        else -> "自定义"
    }
}
