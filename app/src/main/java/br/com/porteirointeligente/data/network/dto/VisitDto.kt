package br.com.porteirointeligente.data.network.dto

import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus

data class VisitDto(
    val id: Long,
    val ownerId: Long,
    val nome: String,
    val documento: String,
    val apartamento: String,
    val telefone: String,
    val motivo: String,
    val dataEntrada: Long,
    val dataSaida: Long?,
    val status: String
) {
    fun toDomain(): Visit {
        return Visit(
            id = id,
            nome = nome,
            documento = documento,
            apartamento = apartamento,
            telefone = telefone,
            motivo = motivo,
            dataEntrada = dataEntrada,
            dataSaida = dataSaida,
            status = VisitStatus.valueOf(status)
        )
    }
}