package ph.edu.wvsu.landas.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a", Locale.US)
private val shortTimeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.US)

fun Long.formatTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(timeFormatter)

fun Long.formatDate(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(dateFormatter)

fun Long.formatDateTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)

fun Long.formatRelative(): String {
    val now = System.currentTimeMillis()
    val diff = (now - this).coerceAtLeast(0)
    val minutes = diff / 60_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 24 * 60 -> "${minutes / 60} hr ago"
        minutes < 48 * 60 -> "Yesterday"
        else -> formatDate()
    }
}

fun formatMinuteOfDay(minuteOfDay: Int): String {
    val hour = (minuteOfDay / 60).coerceIn(0, 23)
    val minute = minuteOfDay % 60
    return LocalDateTime.of(2000, 1, 1, hour, minute).format(timeFormatter)
}

fun formatMinuteShort(minuteOfDay: Int): String {
    val hour = (minuteOfDay / 60).coerceIn(0, 23)
    val minute = minuteOfDay % 60
    return LocalDateTime.of(2000, 1, 1, hour, minute).format(shortTimeFormatter)
}

fun dayLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Monday"
    DayOfWeek.TUESDAY -> "Tuesday"
    DayOfWeek.WEDNESDAY -> "Wednesday"
    DayOfWeek.THURSDAY -> "Thursday"
    DayOfWeek.FRIDAY -> "Friday"
    DayOfWeek.SATURDAY -> "Saturday"
    DayOfWeek.SUNDAY -> "Sunday"
}

fun todayGreeting(): String {
    val hour = LocalDateTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

fun formatPercent(value: Int): String = "$value%"

fun initials(name: String): String {
    return name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}

fun todayDateLabel(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US))
