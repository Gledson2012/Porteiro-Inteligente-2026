package br.com.porteirointeligente.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDataCryptoTest {

    private val crypto = LocalDataCrypto()

    @Test
    fun encryptAndDecryptRoundTrip() {
        val plainText = "Telefone: 5511999998888"
        val encrypted = crypto.encryptText(plainText)

        assertNotEquals(plainText, encrypted)
        assertEquals(plainText, crypto.decryptText(encrypted))
    }

    @Test
    fun legacyPlainTextRemainsReadableForMigration() {
        val plainText = "registro legado"
        assertEquals(plainText, crypto.decryptText(plainText))
    }
}
