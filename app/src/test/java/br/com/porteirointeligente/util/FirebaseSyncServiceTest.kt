package br.com.porteirointeligente.util

import android.content.ContentResolver
import android.content.Context
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários do FirebaseSyncService.
 *
 * Testes de sincronização (syncAll/syncOwners/syncVisits) requerem o Firebase
 * totalmente inicializado (google-services.json válido + Android Runtime) e devem
 * ser executados como testes instrumentados em dispositivo/emulador.
 *
 * Aqui testamos apenas a lógica que independe do Firebase SDK.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseSyncServiceTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var visitRepository: VisitRepository

    private lateinit var firebaseSyncService: FirebaseSyncService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.contentResolver } returns contentResolver

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        coEvery { visitRepository.observeAllVisits() } returns flowOf(emptyList())

        firebaseSyncService = FirebaseSyncService(context, ownerRepository, visitRepository)
    }

    @Test
    fun `initial status should be IDLE`() {
        val status = firebaseSyncService.syncStatus.value
        assert(status is SyncStatus.IDLE) {
            "Expected IDLE but got ${status::class.simpleName}"
        }
    }

    @Test
    fun `resetSyncStatus should set status back to IDLE`() {
        firebaseSyncService.resetSyncStatus()
        val status = firebaseSyncService.syncStatus.value
        assert(status is SyncStatus.IDLE) {
            "Expected IDLE after reset"
        }
    }

    @Test
    fun `getJsonSchemaExample should return valid JSON structure`() {
        val schema = firebaseSyncService.getJsonSchemaExample()
        assert(schema.isNotBlank()) { "Schema should not be blank" }
        assert(schema.contains("\"owners\"")) { "Schema should contain owners key" }
        assert(schema.contains("\"visits\"")) { "Schema should contain visits key" }
        assert(schema.contains("\"qrCodePayload\"")) { "Schema should contain qrCodePayload" }
        assert(schema.contains("porteiro-inteligente.web.app")) { "Schema should contain the LGPD URL" }
    }
}
