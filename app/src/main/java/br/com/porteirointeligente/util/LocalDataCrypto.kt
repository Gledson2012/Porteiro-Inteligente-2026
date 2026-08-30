package br.com.porteirointeligente.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.local.entity.VisitEntity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Protege os campos pessoais persistidos pelo Room.
 *
 * Registros antigos em texto puro continuam legíveis para permitir a migração
 * gradual; qualquer nova inserção ou alteração passa a ser cifrada.
 */
@Singleton
class LocalDataCrypto @Inject constructor() {
    companion object {
        private const val KEY_ALIAS = "porteiro_inteligente_local_data_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH_BYTES = 12
        private const val TAG_LENGTH_BITS = 128
        private const val PREFIX = "v1:"
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return keyStore.getKey(KEY_ALIAS, null) as SecretKey
        }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    fun encryptText(value: String): String {
        if (value.startsWith(PREFIX)) return value
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return PREFIX + Base64Url.encode(cipher.iv + encrypted)
    }

    fun decryptText(value: String): String {
        if (!value.startsWith(PREFIX)) return value
        val combined = Base64Url.decode(value.removePrefix(PREFIX))
        require(combined.size > IV_LENGTH_BYTES) { "Dados locais inválidos" }
        val iv = combined.copyOfRange(0, IV_LENGTH_BYTES)
        val encrypted = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(TAG_LENGTH_BITS, iv)
            )
        }
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    fun encryptOwner(owner: OwnerEntity): OwnerEntity = owner.copy(
        nome = encryptText(owner.nome),
        nomeCondominio = encryptText(owner.nomeCondominio),
        endereco = encryptText(owner.endereco),
        cep = encryptText(owner.cep),
        apartamento = encryptText(owner.apartamento),
        telefone = encryptText(owner.telefone),
        photoUri = owner.photoUri?.let(::encryptText),
        qrCodePayload = encryptText(owner.qrCodePayload),
        offlineMessage = encryptText(owner.offlineMessage)
    )

    fun decryptOwner(owner: OwnerEntity): OwnerEntity = owner.copy(
        nome = decryptText(owner.nome),
        nomeCondominio = decryptText(owner.nomeCondominio),
        endereco = decryptText(owner.endereco),
        cep = decryptText(owner.cep),
        apartamento = decryptText(owner.apartamento),
        telefone = decryptText(owner.telefone),
        photoUri = owner.photoUri?.let(::decryptText),
        qrCodePayload = decryptText(owner.qrCodePayload),
        offlineMessage = decryptText(owner.offlineMessage)
    )

    fun encryptVisit(visit: VisitEntity): VisitEntity = visit.copy(
        nome = encryptText(visit.nome),
        documento = encryptText(visit.documento),
        apartamento = encryptText(visit.apartamento),
        telefone = encryptText(visit.telefone),
        motivo = encryptText(visit.motivo)
    )

    fun decryptVisit(visit: VisitEntity): VisitEntity = visit.copy(
        nome = decryptText(visit.nome),
        documento = decryptText(visit.documento),
        apartamento = decryptText(visit.apartamento),
        telefone = decryptText(visit.telefone),
        motivo = decryptText(visit.motivo)
    )
}
