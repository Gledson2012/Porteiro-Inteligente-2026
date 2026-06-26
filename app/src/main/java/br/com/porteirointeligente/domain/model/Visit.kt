package br.com.porteirointeligente.domain.model

import br.com.porteirointeligente.data.network.dto.VisitDto

/**
 * Representa uma visita registrada no aplicativo.
 *
 * Esta é a entidade de domínio (camada domain). A persistência em Room é feita
 * através de [br.com.porteirointeligente.data.local.entity.VisitEntity], que
 * é convertida em [Visit] pelo repositório.
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
) {
    fun toDto(): VisitDto {
        return VisitDto(
            id = id,
            ownerId = 0, // O ownerId precisa ser gerenciado
            nome = nome,
            documento = documento,
            apartamento = apartamento,
            telefone = telefone,
            motivo = motivo,
            dataEntrada = dataEntrada,
            dataSaida = dataSaida,
            status = status.name
        )
    }
}

/**
 * Status possíveis de uma visita.
 */
enum class VisitStatus {
    ENTRADA_REGISTRADA,
    SAIDA_REGISTRADA,
    CANCELADA
}
