package br.com.porteirointeligente.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineCryptoHelperTest {

    @Test
    fun `encryptOwnerData generates valid v2 hybrid payload with 5 parts`() {
        val payload = OfflineCryptoHelper.encryptOwnerData(
            ownerId = 42L,
            phone = "11999998888",
            name = "Morador Teste",
            isOffline = false,
            offlineMessage = ""
        )

        assertNotNull("Payload não deve ser nulo", payload)
        val parts = payload!!.split(".")
        assertEquals("Deve possuir exatamente 5 partes separadas por ponto", 5, parts.size)
        assertEquals("Prefixo de versão deve ser v2", "v2", parts[0])
        assertEquals("Identificador do morador deve corresponder", "42", parts[1])
        assertTrue("Chave cifrada deve ser não-vazia", parts[2].isNotEmpty())
        assertTrue("IV deve ser não-vazio", parts[3].isNotEmpty())
        assertTrue("Ciphertext e tag devem ser não-vazios", parts[4].isNotEmpty())

        // Verifica que todos os caracteres são URL-safe
        val urlSafePattern = Regex("^[A-Za-z0-9_.-]+$")
        assertTrue("Payload deve ser estritamente URL-safe", urlSafePattern.matches(payload))
    }

    @Test
    fun `encryptOwnerData supports offline status and expiration epoch`() {
        val expireEpoch = System.currentTimeMillis() + 3600000
        val payload = OfflineCryptoHelper.encryptOwnerData(
            ownerId = 100L,
            phone = "11988887777",
            name = "Morador Ausente",
            isOffline = true,
            offlineMessage = "Deixar encomenda no vizinho",
            offlineUntil = expireEpoch
        )

        assertNotNull(payload)
        val parts = payload!!.split(".")
        assertEquals("100", parts[1])
    }

    @Test
    fun `encryptOwnerData returns null for invalid ownerId`() {
        val payloadZero = OfflineCryptoHelper.encryptOwnerData(
            ownerId = 0L,
            phone = "11999998888",
            name = "Invalido",
            isOffline = false,
            offlineMessage = ""
        )
        assertNull("OwnerId 0 não deve gerar QR Code", payloadZero)

        val payloadNegative = OfflineCryptoHelper.encryptOwnerData(
            ownerId = -5L,
            phone = "11999998888",
            name = "Invalido",
            isOffline = false,
            offlineMessage = ""
        )
        assertNull("OwnerId negativo não deve gerar QR Code", payloadNegative)
    }
}
