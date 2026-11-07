package br.com.porteirointeligente.util

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

class OfflineCryptoHelperRoundTripTest {

    @Test
    fun `payload gerado pelo app e decifrado ponta a ponta com a chave privada correspondente`() {
        // Gera um par de chaves RSA próprio para o teste, simulando o par
        // produção (pública embutida no app, privada no backend).
        val keyPair = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        val publicKeyDerBase64 = Base64Url.encode(keyPair.public.encoded)

        val ownerId = 42L
        val phone = "11999998888"
        val name = "Morador Teste"
        val offlineMessage = "Deixar encomenda no vizinho"
        val offlineUntil = 1_700_000_000_000L

        val payload = OfflineCryptoHelper.encryptOwnerData(
            ownerId = ownerId,
            phone = phone,
            name = name,
            isOffline = true,
            offlineMessage = offlineMessage,
            offlineUntil = offlineUntil,
            publicKeyDerBase64 = publicKeyDerBase64
        )

        assertNotNull("Payload não deve ser nulo", payload)
        val parts = payload!!.split(".")
        assertEquals("Envelope deve ter 5 partes", 5, parts.size)
        assertEquals("v2", parts[0])
        assertEquals(ownerId.toString(), parts[1])

        // --- Lado do backend: decifra o envelope híbrido RSA-OAEP + AES-GCM ---
        val encryptedKey = Base64Url.decode(parts[2])
        val iv = Base64Url.decode(parts[3])
        val ciphertext = Base64Url.decode(parts[4])

        val oaepSpec = OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
        )
        val aesKeyBytes = Cipher.getInstance("RSA/ECB/OAEPPadding").run {
            init(Cipher.DECRYPT_MODE, keyPair.private, oaepSpec)
            doFinal(encryptedKey)
        }
        assertEquals("Chave AES deve ter 32 bytes", 32, aesKeyBytes.size)

        val plainJson = Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(aesKeyBytes, "AES"), GCMParameterSpec(128, iv))
            String(doFinal(ciphertext), Charsets.UTF_8)
        }

        val json = JsonParser.parseString(plainJson).asJsonObject
        assertEquals(ownerId, json.get("i").asLong)
        assertEquals(phone, json.get("p").asString)
        assertEquals(name, json.get("n").asString)
        assertEquals(1, json.get("o").asInt)
        assertEquals(offlineMessage, json.get("m").asString)
        assertEquals(offlineUntil, json.get("u").asLong)
    }

    @Test
    fun `payload do app tem formato aceito pelo backend v2`() {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val payload = OfflineCryptoHelper.encryptOwnerData(
            ownerId = 7L,
            phone = "11900000000",
            name = "X",
            isOffline = false,
            offlineMessage = "",
            publicKeyDerBase64 = Base64Url.encode(keyPair.public.encoded)
        )!!

        // O backend espera v2.<id>.<key>.<iv>.<ct> com id numérico positivo.
        val parts = payload.split(".")
        assertEquals(5, parts.size)
        assertTrue(parts[1].toLong() > 0L)
        // id da URL deve coincidir com o id autenticado (verificado pelo backend).
        assertTrue(parts[2].isNotEmpty() && parts[3].isNotEmpty() && parts[4].isNotEmpty())
    }
}
