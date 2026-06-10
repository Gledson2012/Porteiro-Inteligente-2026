package br.com.porteirointeligente.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.porteirointeligente.domain.model.Owner

/**
 * Entidade de Room que persiste um [Owner] no banco de dados local.
 */
@Entity(tableName = "owners")
data class OwnerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nome: String,
    val nomeCondominio: String = "",
    val endereco: String,
    val cep: String = "",
    val apartamento: String,
    val telefone: String,
    val photoUri: String? = null,
    val qrCodePayload: String,
    val dataCadastro: Long,
    val isOffline: Boolean = false,
    val offlineMessage: String = "",
    val offlineUntil: Long? = null
) {
    /**
     * Converte a entidade em modelo de domínio.
     */
    fun toDomain(): Owner = Owner(
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

    companion object {
        /**
         * Converte um modelo de domínio em entidade.
         */
        fun fromDomain(owner: Owner): OwnerEntity = OwnerEntity(
            id = owner.id,
            nome = owner.nome,
            nomeCondominio = owner.nomeCondominio,
            endereco = owner.endereco,
            cep = owner.cep,
            apartamento = owner.apartamento,
            telefone = owner.telefone,
            photoUri = owner.photoUri,
            qrCodePayload = owner.qrCodePayload,
            dataCadastro = owner.dataCadastro,
            isOffline = owner.isOffline,
            offlineMessage = owner.offlineMessage,
            offlineUntil = owner.offlineUntil
        )
    }
}
