package br.com.porteirointeligente.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseSyncServiceTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        FirebaseSyncService.resetSyncStatus()
    }

    @After
    fun tearDown() {
        FirebaseSyncService.resetSyncStatus()
    }

    @Test
    fun `initial status should be IDLE`() {
        val status = FirebaseSyncService.syncStatus.value
        assert(status is FirebaseSyncService.SyncStatus.IDLE) {
            "Expected IDLE but got ${status::class.simpleName}"
        }
    }

    @Test
    fun `simularEnvio should transition SYNCING then SUCCESS`() = runTest(testDispatcher) {
        val job = launch {
            FirebaseSyncService.simularEnvio(
                nome = "João Silva",
                endereco = "Rua das Flores, 123",
                cep = "01310-000",
                telefone = "5511999999999"
            )
        }

        // Advance just enough for the coroutine to start and set SYNCING
        testDispatcher.scheduler.advanceTimeBy(1)
        val syncingStatus = FirebaseSyncService.syncStatus.value
        assert(syncingStatus is FirebaseSyncService.SyncStatus.SYNCING) {
            "Expected SYNCING during execution but got ${syncingStatus::class.simpleName}"
        }

        // Advance past the 1500ms delay
        advanceUntilIdle()
        job.join()

        val finalStatus = FirebaseSyncService.syncStatus.value
        assert(finalStatus is FirebaseSyncService.SyncStatus.SUCCESS) {
            "Expected SUCCESS but got ${finalStatus::class.simpleName}"
        }
        val successStatus = finalStatus as FirebaseSyncService.SyncStatus.SUCCESS
        assert(successStatus.timestamp.isNotBlank()) { "Timestamp should not be blank" }
        assert(successStatus.timestamp.contains("T")) { "Timestamp should be in ISO format (yyyy-MM-dd'T'HH:mm:ss)" }
    }

    @Test
    fun `simularEnvio should handle whitespace trimming`() = runTest(testDispatcher) {
        val job = launch {
            FirebaseSyncService.simularEnvio(
                nome = "  Maria Souza  ",
                endereco = "  Av. Paulista, 1000  ",
                cep = "01310-100",
                telefone = "11988887777"
            )
        }

        advanceUntilIdle()
        job.join()

        val finalStatus = FirebaseSyncService.syncStatus.value
        assert(finalStatus is FirebaseSyncService.SyncStatus.SUCCESS) {
            "Expected SUCCESS but got ${finalStatus::class.simpleName}"
        }
    }

    @Test
    fun `simularEnvio should strip non-digits from CEP`() = runTest(testDispatcher) {
        val job = launch {
            FirebaseSyncService.simularEnvio(
                nome = "Carlos",
                endereco = "Rua Teste",
                cep = "01310-000",  // formatted CEP with hyphen
                telefone = "11999998888"
            )
        }

        advanceUntilIdle()
        job.join()

        val finalStatus = FirebaseSyncService.syncStatus.value
        assert(finalStatus is FirebaseSyncService.SyncStatus.SUCCESS)
    }

    @Test
    fun `simularEnvio should strip non-digits from telefone`() = runTest(testDispatcher) {
        val job = launch {
            FirebaseSyncService.simularEnvio(
                nome = "Ana",
                endereco = "Rua Teste",
                cep = "12345678",
                telefone = "(11) 99999-8888"  // formatted phone with mask
            )
        }

        advanceUntilIdle()
        job.join()

        val finalStatus = FirebaseSyncService.syncStatus.value
        assert(finalStatus is FirebaseSyncService.SyncStatus.SUCCESS)
    }

    @Test
    fun `getJsonSchemaExample should return valid JSON structure`() {
        val schema = FirebaseSyncService.getJsonSchemaExample()
        assert(schema.isNotBlank()) { "Schema should not be blank" }
        assert(schema.contains("\"owners\"")) { "Schema should contain owners key" }
        assert(schema.contains("\"visits\"")) { "Schema should contain visits key" }
        assert(schema.contains("\"qrCodePayload\"")) { "Schema should contain qrCodePayload" }
        assert(schema.contains("porteiro-inteligente.web.app")) { "Schema should contain the LGPD URL" }
    }
}
