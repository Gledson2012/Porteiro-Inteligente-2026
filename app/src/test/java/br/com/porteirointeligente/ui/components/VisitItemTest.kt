package br.com.porteirointeligente.ui.components

import br.com.porteirointeligente.domain.model.VisitStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitItemTest {

    @Test
    fun extractInitials_withMultipleWords_returnsFirstTwoLettersUppercase() {
        assertEquals("CE", extractInitials("Carlos Eduardo Santos"))
        assertEquals("JS", extractInitials("joão silva"))
        assertEquals("AP", extractInitials("Ana Paula"))
    }

    @Test
    fun extractInitials_withSingleWord_returnsSingleLetter() {
        assertEquals("M", extractInitials("Mariana"))
    }

    @Test
    fun extractInitials_withEmptyOrBlankString_returnsDefaultV() {
        assertEquals("V", extractInitials(""))
        assertEquals("V", extractInitials("   "))
    }

    @Test
    fun visitStatus_displayName_returnsCorrectPortugueseLabels() {
        assertEquals("No local", VisitStatus.ENTRADA_REGISTRADA.displayName())
        assertEquals("Concluída", VisitStatus.SAIDA_REGISTRADA.displayName())
        assertEquals("Cancelada", VisitStatus.CANCELADA.displayName())
    }

    @Test
    fun formatRelativeTime_returnsAccurateIntervals() {
        val now = 1720000000000L

        // Future or identical
        assertEquals("Agora", formatRelativeTime(now + 1000L, now = now))

        // Less than 1 min
        assertEquals("Agora há pouco", formatRelativeTime(now - 30_000L, now = now))

        // Minutes
        assertEquals("Há 15 min", formatRelativeTime(now - 15 * 60 * 1000L, now = now))

        // Hours
        assertEquals("Há 4h", formatRelativeTime(now - 4 * 3600 * 1000L, now = now))

        // Yesterday (1 day)
        assertEquals("Ontem", formatRelativeTime(now - 24 * 3600 * 1000L, now = now))

        // Few days
        assertEquals("Há 3 dias", formatRelativeTime(now - 3 * 24 * 3600 * 1000L, now = now))

        // More than a week
        val older = now - 10 * 24 * 3600 * 1000L
        val formatted = formatRelativeTime(older, now = now)
        assertTrue(formatted.contains("às"))
    }

    @Test
    fun formatDateTime_returnsStandardBrazilianFormat() {
        val timestamp = 1720000000000L
        val formatted = formatDateTime(timestamp)
        assertTrue(formatted.matches(Regex("\\d{2}/\\d{2}/\\d{4} às \\d{2}:\\d{2}")))
    }
}
