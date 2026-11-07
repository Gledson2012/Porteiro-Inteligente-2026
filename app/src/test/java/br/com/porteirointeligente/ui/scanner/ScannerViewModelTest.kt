package br.com.porteirointeligente.ui.scanner

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.CryptoUtil
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var cryptoUtil: CryptoUtil

    private lateinit var viewModel: ScannerViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        every { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        coEvery { ownerRepository.getOwnerById(any()) } returns null

        viewModel = ScannerViewModel(ownerRepository, cryptoUtil)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onQrCodeDetected with generic wa-me link should emit OpenWhatsApp event`() = runTest(testDispatcher) {
        val testUrl = "https://wa.me/5511999998888?text=Olá"
        val events = mutableListOf<ScannerViewModel.ScannerUiEvent>()
        val job = launch {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.onQrCodeDetected(testUrl)
        advanceUntilIdle()

        assert(events.isNotEmpty()) { "Expected events to be emitted" }
        val event = events.first()
        assert(event is ScannerViewModel.ScannerUiEvent.OpenWhatsApp)
        assert((event as ScannerViewModel.ScannerUiEvent.OpenWhatsApp).url == testUrl)
        job.cancel()
    }

    @Test
    fun `onQrCodeDetected with invalid QR Code should emit InvalidQrCode event`() = runTest(testDispatcher) {
        val events = mutableListOf<ScannerViewModel.ScannerUiEvent>()
        val job = launch {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.onQrCodeDetected("https://invalid-url.com")
        advanceUntilIdle()

        assert(events.isNotEmpty()) { "Expected events to be emitted" }
        val event = events.first()
        assert(event is ScannerViewModel.ScannerUiEvent.InvalidQrCode)
        job.cancel()
    }

    @Test
    fun `v2 link without local match forwards the encrypted URL to the backend`() = runTest(testDispatcher) {
        // decryptOwnerData retorna null em produção: o app não guarda a chave
        // privada do QR. Sem morador local com o mesmo payload, o backend valida.
        val url = "https://porteiro-inteligente-2026.vercel.app/scan/v2.1.payload-sem-correspondencia"

        val events = mutableListOf<ScannerViewModel.ScannerUiEvent>()
        val job = launch { viewModel.uiEvent.collect { events.add(it) } }

        viewModel.onQrCodeDetected(url)
        advanceUntilIdle()

        val event = events.first()
        assert(event is ScannerViewModel.ScannerUiEvent.OpenWhatsApp)
        assert((event as ScannerViewModel.ScannerUiEvent.OpenWhatsApp).url == url)
        job.cancel()
    }

    @Test
    fun `v2 link matching the stored payload uses local phone and offline status`() = runTest(testDispatcher) {
        val url = "https://porteiro-inteligente-2026.vercel.app/scan/v2.1.payload-local"
        val savedOwner = Owner(
            id = 1L,
            nome = "Morador Local",
            endereco = "Rua 1",
            apartamento = "42",
            telefone = "11988887777",
            qrCodePayload = url,
            isOffline = true,
            offlineMessage = "Estou ausente no momento"
        )
        coEvery { ownerRepository.getOwnerById(1L) } returns savedOwner

        val events = mutableListOf<ScannerViewModel.ScannerUiEvent>()
        val job = launch { viewModel.uiEvent.collect { events.add(it) } }

        viewModel.onQrCodeDetected(url)
        advanceUntilIdle()

        val event = events.first()
        assert(event is ScannerViewModel.ScannerUiEvent.ShowOfflineMessage)
        val offlineEvent = event as ScannerViewModel.ScannerUiEvent.ShowOfflineMessage
        assert(offlineEvent.message == "Estou ausente no momento")
        assert(offlineEvent.url.contains("5511988887777"))
        job.cancel()
    }

    @Test
    fun `onQrCodeDetected with forged owner id must not use local phone`() = runTest(testDispatcher) {
        val savedOwner = Owner(
            id = 1L,
            nome = "Morador Local",
            endereco = "Rua 1",
            apartamento = "42",
            telefone = "11988887777",
            qrCodePayload = "https://porteiro-inteligente-2026.vercel.app/scan/v2.1.valid-payload"
        )
        coEvery { ownerRepository.getOwnerById(1L) } returns savedOwner

        val forgedUrl = "https://porteiro-inteligente-2026.vercel.app/scan/v2.1.forged-payload"
        val events = mutableListOf<ScannerViewModel.ScannerUiEvent>()
        val job = launch { viewModel.uiEvent.collect { events.add(it) } }

        viewModel.onQrCodeDetected(forgedUrl)
        advanceUntilIdle()

        assert(events.first() is ScannerViewModel.ScannerUiEvent.OpenWhatsApp)
        assert((events.first() as ScannerViewModel.ScannerUiEvent.OpenWhatsApp).url == forgedUrl)
        job.cancel()
    }
}
