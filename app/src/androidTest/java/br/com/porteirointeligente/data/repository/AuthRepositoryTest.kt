package br.com.porteirointeligente.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.porteirointeligente.util.LocalDataCrypto
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository

    @Before
    fun setUp() = runBlocking {
        repository = AuthRepository(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            LocalDataCrypto()
        )
        repository.deleteAccount()
    }

    @After
    fun tearDown() = runBlocking {
        repository.deleteAccount()
    }

    @Test
    fun registerAndLoginWithCorrectPassword() = runBlocking {
        assertTrue(repository.register("usuario-teste", "senha-segura-123").isSuccess)
        assertTrue(repository.login("usuario-teste", "senha-segura-123").isSuccess)
    }

    @Test
    fun loginRejectsWrongPassword() = runBlocking {
        assertTrue(repository.register("usuario-teste", "senha-segura-123").isSuccess)
        assertFalse(repository.login("usuario-teste", "senha-incorreta").isSuccess)
    }
}
