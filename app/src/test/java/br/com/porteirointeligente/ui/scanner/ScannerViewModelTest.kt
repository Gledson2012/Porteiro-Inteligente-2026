package br.com.porteirointeligente.ui.scanner

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.util.CryptoUtil
import br.com.porteirointeligente.util.OfflineCryptoHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONObject
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
        mockkObject(OfflineCryptoHelper)
        
        every { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        coEvery { ownerRepository.getOwnerById(any()) } returns null
        
        viewModel = ScannerViewModel(ownerRepository, cryptoUtil)
    }

    @After
    fun tearDown() {
        unmockkObject(OfflineCryptoHelper)
        Dispatchers.resetMain()
    }

    @Test
    fun `onQrCodeDetected with generic wa-me link should emit OpenWhatsApp event and not load local owners`() = runTest(testDispatcher) {
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
        every { OfflineCryptoHelper.decryptOwnerData(any()) } returns null
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
    fun `onQrCodeDetected with new LGPD link and online owner should emit OpenWhatsApp`() = runTest(testDispatcher) {
        val idPart = "some_encrypted_data"
        val url = "https://porteiro-inteligente-2026.vercel.app/scan/$idPart"
        
        val mockJson = mockk<JSONObject>()
        every { mockJson.optString("n", "") } returns "João Silva"
        every { mockJson.optString("p", "") } returns "11999998888"
        every { mockJson.optInt("o", 0) } returns 0
        every { mockJson.optString("m", "") } returns ""
        
        every { OfflineCryptoHelper.decryptOwnerData(idPart) } returns mockJson
        
        val events = mutableListOf<ScannerViewModel.ScannerUiEvent>()
        val job = launch {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.onQrCodeDetected(url)
        advanceUntilIdle()
        
        assert(events.isNotEmpty()) { "Expected events to be emitted" }
        val event = events.first()
        assert(event is ScannerViewModel.ScannerUiEvent.OpenWhatsApp)
        assert((event as ScannerViewModel.ScannerUiEvent.OpenWhatsApp).url.contains("5511999998888"))
        job.cancel()
    }

    @Test
    fun `onQrCodeDetected with new LGPD link and offline owner should emit ShowOfflineMessage`() = runTest(testDispatcher) {
        val idPart = "some_encrypted_data"
        val url = "https://porteiro-inteligente-2026.vercel.app/scan/$idPart"
        
        val mockJson = mockk<JSONObject>()
        every { mockJson.optString("n", "") } returns "João Silva"
        every { mockJson.optString("p", "") } returns "11999998888"
        every { mockJson.optInt("o", 0) } returns 1
        every { mockJson.optString("m", "") } returns "Estou ausente no momento"
        
        every { OfflineCryptoHelper.decryptOwnerData(idPart) } returns mockJson
        
        val events = mutableListOf<ScannerViewModel.ScannerUiEvent>()
        val job = launch {
            viewModel.uiEvent.collect { events.add(it) }
        }

        viewModel.onQrCodeDetected(url)
        advanceUntilIdle()
        
        assert(events.isNotEmpty()) { "Expected events to be emitted" }
        val event = events.first()
        assert(event is ScannerViewModel.ScannerUiEvent.ShowOfflineMessage)
        val showOfflineEvent = event as ScannerViewModel.ScannerUiEvent.ShowOfflineMessage
        assert(showOfflineEvent.message == "Estou ausente no momento")
        assert(showOfflineEvent.url.contains("5511999998888"))
        job.cancel()
    }
}
