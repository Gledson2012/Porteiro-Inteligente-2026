package br.com.porteirointeligente.data.network.dto

import br.com.porteirointeligente.domain.model.Owner

data class OwnerDto(
    val id: Long,
    val userId: Long,
    val nome: String,
    val nomeCondominio: String,
    val endereco: String,
    val cep: String,
    val apartamento: String,
    val telefone: String,
    val photoUri: String?,
    val qrCodePayload: String,
    val dataCadastro: Long,
    val isOffline: Boolean,
    val offlineMessage: String,
    val offlineUntil: Long?
) {
    fun toDomain(): Owner {
        return Owner(
            id = id,
            nome = nome,
            nomeCondominio = nomeCondominio,
            endereco = endereco,
            cep = cep,
            apartamento = apartamento,
            telefone = telefone,
            photoUri = photoUri,
            qrCodePayload = qrCodePayload,
            dataCadastro = dataCadastro,
            isOffline = isOffline,
            offlineMessage = offlineMessage,
            offlineUntil = offlineUntil
        )
    }
}