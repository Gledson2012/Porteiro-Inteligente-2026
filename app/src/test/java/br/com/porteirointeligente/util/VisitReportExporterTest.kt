package br.com.porteirointeligente.util

import br.com.porteirointeligente.domain.model.Visit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitReportExporterTest {

    private fun visit(nome: String, documento: String = "123", motivo: String = "Entrega") = Visit(
        id = 1L,
        nome = nome,
        documento = documento,
        apartamento = "42",
        telefone = "11999998888",
        motivo = motivo,
        dataEntrada = 1_700_000_000_000L
    )

    @Test
    fun `neutralizes spreadsheet formulas in text fields`() {
        val csv = VisitReportExporter.generateCsv(
            listOf(visit(nome = "=HYPERLINK(\"http://evil\")", documento = "+55 11 99999-8888"))
        )

        assertTrue(csv.contains("'=HYPERLINK"))
        assertTrue(csv.contains(";'+55 11 99999-8888"))
    }

    @Test
    fun `keeps regular values untouched`() {
        val csv = VisitReportExporter.generateCsv(listOf(visit(nome = "Maria Silva")))

        assertTrue(csv.contains(";Maria Silva;"))
        assertFalse(csv.contains("'Maria Silva"))
    }
}
