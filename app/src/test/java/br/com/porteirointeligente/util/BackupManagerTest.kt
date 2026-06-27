package br.com.porteirointeligente.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.After
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class BackupManagerTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var cryptoUtil: CryptoUtil

    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>()
        every { Uri.parse(any()) } returns mockUri

        every { context.contentResolver } returns contentResolver
        every { cryptoUtil.encrypt(any()) } answers { firstArg() }
        every { cryptoUtil.decrypt(any()) } answers { firstArg() }

        val testOwner = Owner(
            id = 1L,
            nome = "João Silva",
            endereco = "Rua 1",
            apartamento = "42",
            telefone = "5511999998888",
            qrCodePayload = "teste"
        )

        val testVisit = Visit(
            id = 1L,
            nome = "Carlos",
            documento = "RG123",
            apartamento = "42",
            telefone = "11999999999",
            motivo = "Entrega",
            dataEntrada = System.currentTimeMillis(),
            status = br.com.porteirointeligente.domain.model.VisitStatus.ENTRADA_REGISTRADA
        )

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner))
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(testVisit))
        coEvery { ownerRepository.deleteAll() } just runs
        coEvery { visitRepository.clearAll() } just runs
        coEvery { ownerRepository.insertOwner(any()) } returns testOwner
        coEvery { visitRepository.insertVisit(any()) } returns testVisit

        backupManager = BackupManager(context, ownerRepository, visitRepository, cryptoUtil)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `restoreBackup with invalid URI should return false`() = runTest {
        coEvery { contentResolver.openInputStream(any()) } returns null

        val result = backupManager.restoreBackup(Uri.parse("content://invalid"))
        assert(!result) { "Expected false but got $result" }
    }

    @Test
    fun `restoreBackup with valid JSON should return true`() = runTest {
        val json = """
            {
                "version": 2,
                "exportDate": "2026-01-20 14:30:00",
                "owner": null,
                "owners": [
                    {
                        "id": 1,
                        "nome": "João Silva",
                        "nomeCondominio": "",
                        "endereco": "Rua 1",
                        "cep": "",
                        "apartamento": "42",
                        "telefone": "5511999998888",
                        "photoUri": null,
                        "qrCodePayload": "teste",
                        "dataCadastro": 1700000000000,
                        "isOffline": false,
                        "offlineMessage": "",
                        "offlineUntil": null
                    }
                ],
                "visits": [
                    {
                        "id": 1,
                        "nome": "Carlos",
                        "documento": "RG123",
                        "apartamento": "42",
                        "telefone": "11999999999",
                        "motivo": "Entrega",
                        "dataEntrada": 1700000000000,
                        "dataSaida": null,
                        "status": "ENTRADA_REGISTRADA"
                    }
                ]
            }
        """.trimIndent()

        coEvery { contentResolver.openInputStream(any()) } returns ByteArrayInputStream(json.toByteArray())

        val result = backupManager.restoreBackup(Uri.parse("content://teste"))
        assert(result) { "Expected true but got $result" }
    }

    @Test
    fun `restoreBackup with invalid JSON should return false`() = runTest {
        val invalidJson = "invalid json content"

        coEvery { contentResolver.openInputStream(any()) } returns ByteArrayInputStream(invalidJson.toByteArray())

        val result = backupManager.restoreBackup(Uri.parse("content://teste"))
        assert(!result) { "Expected false but got $result" }
    }

    @Test
    fun `restoreBackup with encrypted JSON should decrypt and return true`() = runTest {
        val encryptedJson = "ENCRYPTED_DATA"
        val plainJson = """
            {
                "version": 2,
                "exportDate": "2026-01-20 14:30:00",
                "owner": null,
                "owners": [],
                "visits": []
            }
        """.trimIndent()

        every { cryptoUtil.decrypt(encryptedJson) } returns plainJson
        coEvery { contentResolver.openInputStream(any()) } returns ByteArrayInputStream(encryptedJson.toByteArray())

        val result = backupManager.restoreBackup(Uri.parse("content://teste"))
        assert(result) { "Expected true but got $result" }
    }
}
