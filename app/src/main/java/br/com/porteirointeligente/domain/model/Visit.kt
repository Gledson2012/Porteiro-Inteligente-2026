package br.com.porteirointeligente.domain.model

/**
 * Representa uma visita registrada no aplicativo.
 *
 * Dados persistidos localmente via Room (SQLite).
 */
data class Visit(
    val id: Long = 0L,
    val nome: String,
    val documento: String,
    val apartamento: String,
    val telefone: String,
    val motivo: String,
    val dataEntrada: Long,
    val dataSaida: Long? = null,
    val status: VisitStatus = VisitStatus.ENTRADA_REGISTRADA
)

/**
 * Status possíveis de uma visita.
 */
enum class VisitStatus {
    ENTRADA_REGISTRADA,
    SAIDA_REGISTRADA,
    CANCELADA
}
