package br.com.porteirointeligente.domain.model

import br.com.porteirointeligente.data.network.dto.OwnerDto

/**
 * Representa o morador/proprietário cadastrado no aplicativo.
 */
data class Owner(
    val id: Long = 0L,
    val nome: String,
    val nomeCondominio: String = "",
    val endereco: String,
    val cep: String = "",
    val apartamento: String,
    val telefone: String,
    val photoUri: String? = null,
    val qrCodePayload: String,
    val dataCadastro: Long = System.currentTimeMillis(),
    // Status de disponibilidade (Modo Online/Offline)
    val isOffline: Boolean = false,
    val offlineMessage: String = "",
    val offlineUntil: Long? = null
) {
    fun isCurrentlyOffline(): Boolean {
        if (!isOffline) return false
        val until = offlineUntil ?: return true
        return System.currentTimeMillis() < until
    }

    fun toDto(): OwnerDto {
        return OwnerDto(
            id = id,
            userId = 0, // Será preenchido pelo servidor
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
