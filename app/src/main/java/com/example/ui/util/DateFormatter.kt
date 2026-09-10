package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val monthDayTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun formatModifiedDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val noteDate = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == noteDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == noteDate.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            return "Today, ${timeFormat.format(Date(timestamp))}"
        }

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == noteDate.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == noteDate.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "Yesterday, ${timeFormat.format(Date(timestamp))}"
        }

        val isThisYear = now.get(Calendar.YEAR) == noteDate.get(Calendar.YEAR)
        return if (isThisYear) {
            monthDayFormat.format(Date(timestamp))
        } else {
            fullDateFormat.format(Date(timestamp))
        }
    }

    fun formatDetailDate(timestamp: Long): String {
        return monthDayTimeFormat.format(Date(timestamp))
    }
}
