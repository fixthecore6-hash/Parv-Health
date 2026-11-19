package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private const val ISO_PATTERN = "yyyy-MM-dd"

    fun todayIso(): String = formatCalendar(Calendar.getInstance())

    fun daysAgoIso(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return formatCalendar(cal)
    }

    fun lastSevenDaysIso(): List<String> = lastDaysIso(7)

    fun lastDaysIso(count: Int): List<String> {
        val safeCount = count.coerceAtLeast(1)
        return ((safeCount - 1) downTo 0).map { daysAgoIso(it) }
    }

    /**
     * Returns ISO dates for Monday..Sunday of the current calendar week.
     */
    fun currentWeekDaysIso(): List<String> {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offsetFromMonday = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        cal.add(Calendar.DAY_OF_YEAR, -offsetFromMonday)
        return (0..6).map { index ->
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, index)
            formatCalendar(dayCal)
        }
    }

    /**
     * 0 = Monday .. 6 = Sunday for today.
     */
    fun todayWeekDayIndex(): Int {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    fun shortWeekday(isoDate: String): String {
        val date = parseIso(isoDate) ?: return isoDate
        return SimpleDateFormat("EEE", Locale.getDefault()).format(date).take(3)
    }

    fun singleLetterWeekday(isoDate: String): String {
        val date = parseIso(isoDate) ?: return ""
        return SimpleDateFormat("EEEEE", Locale.getDefault()).format(date)
    }

    fun formatDisplayDate(isoDate: String, todayLabel: String, yesterdayLabel: String): String {
        val today = todayIso()
        val yesterday = daysAgoIso(1)
        return when (isoDate) {
            today -> todayLabel
            yesterday -> yesterdayLabel
            else -> {
                val parsed = parseIso(isoDate) ?: return isoDate
                SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(parsed)
            }
        }
    }

    private fun formatCalendar(calendar: Calendar): String {
        return SimpleDateFormat(ISO_PATTERN, Locale.US).format(calendar.time)
    }

    private fun parseIso(isoDate: String) = runCatching {
        SimpleDateFormat(ISO_PATTERN, Locale.US).parse(isoDate)
    }.getOrNull()
}
