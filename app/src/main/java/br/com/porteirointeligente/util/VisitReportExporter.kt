package br.com.porteirointeligente.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitário para exportar relatórios de visitas em formato CSV
 * com compatibilidade direta para Excel, Google Planilhas e compartilhamento
 * via WhatsApp, e-mail ou armazenamento local.
 */
object VisitReportExporter {

    private val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
    private val fileNameFormatter = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    /**
     * Gera o conteúdo CSV completo com cabeçalho e codificação UTF-8 com BOM.
     */
    fun generateCsv(visits: List<Visit>): String {
        val builder = StringBuilder()
        // Byte Order Mark (BOM) para o Microsoft Excel reconhecer acentos em português automaticamente
        builder.append("\uFEFF")

        // Cabeçalho das colunas
        builder.append("ID;Nome do Visitante;Documento;Apartamento;Placa do Veículo;Telefone;Motivo;Data Entrada;Data Saída;Status\n")

        for (visit in visits) {
            val statusLabel = when (visit.status) {
                VisitStatus.ENTRADA_REGISTRADA -> "No local"
                VisitStatus.SAIDA_REGISTRADA -> "Saída registrada"
                VisitStatus.CANCELADA -> "Cancelada"
            }

            val dataEntrada = dateTimeFormatter.format(Date(visit.dataEntrada))
            val dataSaida = visit.dataSaida?.let { dateTimeFormatter.format(Date(it)) } ?: "—"
            val placa = visit.placa?.takeIf { it.isNotBlank() } ?: "—"

            builder.append(escapeCsv(visit.id.toString())).append(";")
            builder.append(escapeCsv(visit.nome)).append(";")
            builder.append(escapeCsv(visit.documento.ifBlank { "—" })).append(";")
            builder.append(escapeCsv(visit.apartamento)).append(";")
            builder.append(escapeCsv(placa)).append(";")
            builder.append(escapeCsv(visit.telefone.ifBlank { "—" })).append(";")
            builder.append(escapeCsv(visit.motivo.ifBlank { "—" })).append(";")
            builder.append(escapeCsv(dataEntrada)).append(";")
            builder.append(escapeCsv(dataSaida)).append(";")
            builder.append(escapeCsv(statusLabel)).append("\n")
        }

        return builder.toString()
    }

    /**
     * Gera o arquivo CSV e dispara o Intent de compartilhamento do Android.
     */
    fun shareCsvReport(context: Context, visits: List<Visit>) {
        if (visits.isEmpty()) return

        val csvContent = generateCsv(visits)
        val fileName = "Relatorio_Visitas_${fileNameFormatter.format(Date())}.csv"
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { output ->
            output.write(csvContent.toByteArray(StandardCharsets.UTF_8))
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de Visitas — Porteiro Inteligente")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Compartilhar Relatório de Visitas").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        return if (needsQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
