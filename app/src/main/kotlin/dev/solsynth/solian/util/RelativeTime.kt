package dev.solsynth.solian.util

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import android.util.Log
import java.time.format.DateTimeParseException

object RelativeTime {
    fun format(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val instant = parseToInstant(isoString) ?: return ""
            val now = Instant.now()
            val duration = Duration.between(instant, now)
            val absDuration = duration.abs()
            val absSeconds = absDuration.seconds
            
            when {
                absSeconds < 60 -> "${absSeconds}s ago"
                absDuration.toMinutes() < 60 -> "${absDuration.toMinutes()}m ago"
                absDuration.toHours() < 24 -> "${absDuration.toHours()}h ago"
                absDuration.toDays() < 7 -> "${absDuration.toDays()}d ago"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("MMM dd")
                        .withZone(ZoneId.systemDefault())
                    formatter.format(instant)
                }
            }
        } catch (e: Exception) {
            Log.e("RelativeTime", "Error parsing time: $isoString", e)
            ""
        }
    }

    private fun parseToInstant(dateStr: String): Instant? {
        return try {
            // Standard ISO with T and Z
            Instant.parse(dateStr)
        } catch (e: Exception) {
            try {
                // Try format "2026-08-02 04:18:06.364061" -> "2026-08-02T04:18:06.364061Z"
                val iso = dateStr.replace(" ", "T").let { 
                    if (!it.contains("Z") && !it.contains("+")) it + "Z" else it
                }
                Instant.parse(iso)
            } catch (e2: Exception) {
                try {
                    // Try simple date (e.g. "2026-08-02") as start of day UTC
                    java.time.LocalDate.parse(dateStr)
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant()
                } catch (e3: Exception) {
                    null
                }
            }
        }
    }
}
