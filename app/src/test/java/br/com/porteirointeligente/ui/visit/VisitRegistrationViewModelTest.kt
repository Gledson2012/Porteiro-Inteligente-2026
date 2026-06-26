package br.com.porteirointeligente.ui.visit

import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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

    private lateinit var viewModel: VisitRegistrationViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testVisit = Visit(
        id = 1L,
        nome = "João Silva",
        documento = "RG123456",
        apartamento = "42",
        telefone = "11999999999",
        motivo = "Entrega",
        dataEntrada = System.currentTimeMillis(),
        status = VisitStatus.ENTRADA_REGISTRADA
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        coEvery { visitRepository.observeAllVisits() } returns flowOf(emptyList())
        coEvery { visitRepository.insertVisit(any()) } returns Result.success(testVisit)
        
        viewModel = VisitRegistrationViewModel(visitRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `registrarVisita with blank nome should set Error state`() = runTest(testDispatcher) {
        viewModel.registrarVisita("", "RG123", "12", "11999999999", "Visita")
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assert(state is VisitRegistrationUIState.Error)
        assert((state as VisitRegistrationUIState.Error).message == "Nome e apartamento são obrigatórios.")
    }

    @Test
    fun `registrarVisita with blank apartamento should set Error state`() = runTest(testDispatcher) {
        viewModel.registrarVisita("João", "RG123", "", "11999999999", "Visita")
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assert(state is VisitRegistrationUIState.Error)
        assert((state as VisitRegistrationUIState.Error).message == "Nome e apartamento são obrigatórios.")
    }

    @Test
    fun `registrarVisita with valid data should set Success state`() = runTest(testDispatcher) {
        viewModel.registrarVisita("João Silva", "RG123456", "42", "11999999999", "Entrega")
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assert(state is VisitRegistrationUIState.Success)
        coVerify { visitRepository.insertVisit(any()) }
    }
}
