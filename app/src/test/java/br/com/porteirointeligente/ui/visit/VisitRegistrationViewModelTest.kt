package br.com.porteirointeligente.ui.visit

import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.util.NotificationHelper
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.Runs
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
class VisitRegistrationViewModelTest {

    @MockK
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var notificationHelper: NotificationHelper

    private lateinit var viewModel: VisitRegistrationViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        coEvery { visitRepository.observeAllVisits() } returns flowOf(emptyList())
        coEvery { visitRepository.insertVisit(any()) } returns 1L
        coEvery { notificationHelper.showVisitNotification(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `registrarVisita with blank nome should emit ErrorFields`() = runTest(testDispatcher) {
        viewModel = VisitRegistrationViewModel(visitRepository, notificationHelper)
        viewModel.registrarVisita("", "RG123", "12", "11999999999", "Visita")
        advanceUntilIdle()
        // Como SharedFlow não completa, verificamos via StateFlow ou contagem
        // Usamos first() com timeout implícito
        val events = mutableListOf<VisitRegistrationViewModel.VisitUiEvent>()
        val job = launch {
            viewModel.event.collect { events.add(it) }
        }
        advanceUntilIdle()
        job.cancel()
        assert(events.any { it is VisitRegistrationViewModel.VisitUiEvent.ErrorFields })
    }

    @Test
    fun `registrarVisita with blank apartamento should emit ErrorFields`() = runTest(testDispatcher) {
        viewModel = VisitRegistrationViewModel(visitRepository, notificationHelper)
        viewModel.registrarVisita("João", "RG123", "", "11999999999", "Visita")
        advanceUntilIdle()
        val events = mutableListOf<VisitRegistrationViewModel.VisitUiEvent>()
        val job = launch {
            viewModel.event.collect { events.add(it) }
        }
        advanceUntilIdle()
        job.cancel()
        assert(events.any { it is VisitRegistrationViewModel.VisitUiEvent.ErrorFields })
    }

    @Test
    fun `registrarVisita with valid data should emit Success`() = runTest(testDispatcher) {
        viewModel = VisitRegistrationViewModel(visitRepository, notificationHelper)
        viewModel.registrarVisita("João Silva", "RG123456", "42", "11999999999", "Entrega")
        advanceUntilIdle()
        val events = mutableListOf<VisitRegistrationViewModel.VisitUiEvent>()
        val job = launch {
            viewModel.event.collect { events.add(it) }
        }
        advanceUntilIdle()
        job.cancel()
        assert(events.any { it is VisitRegistrationViewModel.VisitUiEvent.Success })
        coVerify { visitRepository.insertVisit(any()) }
        coVerify { notificationHelper.showVisitNotification("João Silva", "42") }
    }
}
