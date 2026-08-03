package dev.solsynth.solian.util

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelativeTimeTest {

    @Test
    fun `blank input returns empty string`() {
        assertEquals("", RelativeTime.format(null))
        assertEquals("", RelativeTime.format(""))
        assertEquals("", RelativeTime.format("   "))
    }

    @Test
    fun `invalid input returns empty string`() {
        assertEquals("", RelativeTime.format("not-a-date"))
        assertEquals("", RelativeTime.format("2026-13-45T99:99:99Z"))
    }

    @Test
    fun `seconds ago formats as seconds`() {
        val input = Instant.now().minus(10, ChronoUnit.SECONDS).toString()
        val output = RelativeTime.format(input)
        assertTrue("expected seconds suffix, got: $output", output.endsWith("s ago"))
    }

    @Test
    fun `minutes ago formats as minutes`() {
        val input = Instant.now().minus(5, ChronoUnit.MINUTES).toString()
        val output = RelativeTime.format(input)
        assertTrue("expected minutes suffix, got: $output", output.endsWith("m ago"))
    }

    @Test
    fun `hours ago formats as hours`() {
        val input = Instant.now().minus(3, ChronoUnit.HOURS).toString()
        val output = RelativeTime.format(input)
        assertTrue("expected hours suffix, got: $output", output.endsWith("h ago"))
    }

    @Test
    fun `days ago formats as days`() {
        val input = Instant.now().minus(2, ChronoUnit.DAYS).toString()
        val output = RelativeTime.format(input)
        assertTrue("expected days suffix, got: $output", output.endsWith("d ago"))
    }

    @Test
    fun `older than a week falls back to date format`() {
        val input = Instant.now().minus(30, ChronoUnit.DAYS).toString()
        val output = RelativeTime.format(input)
        assertTrue("expected date fallback, got: $output", output.matches(Regex("[A-Z][a-z]{2} \\d{1,2}")))
    }

    @Test
    fun `parses server format without timezone suffix`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MICROS)
        val formatted = now.toString().replace("T", " ").removeSuffix("Z")
        val output = RelativeTime.format(formatted)
        assertTrue("expected a relative time, got: $output", output.isNotBlank())
    }

    @Test
    fun `parses simple date input`() {
        val output = RelativeTime.format("2026-08-02")
        assertTrue("expected a parsed value, got: $output", output.isNotBlank())
    }
}
