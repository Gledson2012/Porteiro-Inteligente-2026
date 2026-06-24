package br.com.porteirointeligente.util

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para o [CryptoUtil].
 *
 * Nota: Como o Android Keystore não está disponível em testes unitários
 * (roboletric ou emulador necessário), estes testes verificam
 * apenas o comportamento esperado da classe.
 */
class CryptoUtilTest {

    @MockK
    private lateinit var context: Context

    private lateinit var cryptoUtil: CryptoUtil

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        cryptoUtil = CryptoUtil(context)
    }

    @Test
    fun `encrypt should return null when Keystore is unavailable`() {
        // Em ambiente de teste unitário (sem Android Keystore),
        // o encrypt deve retornar null sem lançar exceção
        val result = cryptoUtil.encrypt("teste")
        assert(result == null) { "Expected null but got $result" }
    }

    @Test
    fun `decrypt should return null when Keystore is unavailable`() {
        val result = cryptoUtil.decrypt("dGVzdGU=")
        assert(result == null) { "Expected null but got $result" }
    }

    @Test
    fun `resetKey should not throw exception when Keystore is unavailable`() {
        try {
            cryptoUtil.resetKey()
            assert(true)
        } catch (e: Exception) {
            assert(false) { "resetKey should not throw: ${e.message}" }
        }
    }
}
