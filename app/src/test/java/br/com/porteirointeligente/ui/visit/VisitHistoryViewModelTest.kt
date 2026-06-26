package br.com.porteirointeligente.ui.visit

import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitHistoryViewModelTest {

    @MockK
    private lateinit var visitRepository: VisitRepository

    private lateinit var viewModel: VisitHistoryViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val activeVisit = Visit(
        id = 1L,
        nome = "Carlos",
        documento = "RG123",
        apartamento = "101",
        telefone = "11999999999",
        motivo = "Entrega",
        dataEntrada = System.currentTimeMillis(),
        status = VisitStatus.ENTRADA_REGISTRADA
    )

    private val completedVisit = Visit(
        id = 2L,
        nome = "Ana",
        documento = "RG456",
        apartamento = "202",
        telefone = "11988888888",
        motivo = "Visita",
        dataEntrada = System.currentTimeMillis() - 3600000,
        dataSaida = System.currentTimeMillis(),
        status = VisitStatus.SAIDA_REGISTRADA
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // flow {} (empty lambda) never emits, keeping ViewModel in Loading state
        coEvery { visitRepository.observeAllVisits() } returns flow { }

        viewModel = VisitHistoryViewModel(visitRepository)

        assert(viewModel.uiState.value is VisitHistoryUIState.Loading) {
            "Expected Loading state initially, got ${viewModel.uiState.value::class.simpleName}"
        }
    }

    @Test
    fun `loadVisits with ALL filter should emit Success with all visits`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit, completedVisit))

        viewModel = VisitHistoryViewModel(visitRepository)

        val state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.visits?.size == 2) { "Expected 2 visits, got ${state?.visits?.size}" }
        assert(state?.filter == VisitHistoryViewModel.Filter.ALL) { "Expected ALL filter" }
    }

    @Test
    fun `setFilter to ACTIVE should filter visits by ENTRADA_REGISTRADA`() {
        val allVisitsFlow = MutableStateFlow(listOf(activeVisit, completedVisit))

        coEvery { visitRepository.observeAllVisits() } returns allVisitsFlow
        coEvery { visitRepository.observeVisitsByStatus(VisitStatus.ENTRADA_REGISTRADA) } returns flowOf(listOf(activeVisit))

        viewModel = VisitHistoryViewModel(visitRepository)

        // Wait for initial load
        viewModel.setFilter(VisitHistoryViewModel.Filter.ACTIVE)

        val state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.filter == VisitHistoryViewModel.Filter.ACTIVE) { "Expected ACTIVE filter" }
        assert(state?.visits?.size == 1) { "Expected 1 visit after filtering, got ${state?.visits?.size}" }
        assert(state?.visits?.first()?.status == VisitStatus.ENTRADA_REGISTRADA) { "Expected active visit only" }
    }

    @Test
    fun `setFilter back to ALL should show all visits`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit, completedVisit))
        coEvery { visitRepository.observeVisitsByStatus(VisitStatus.ENTRADA_REGISTRADA) } returns flowOf(listOf(activeVisit))

        viewModel = VisitHistoryViewModel(visitRepository)

        viewModel.setFilter(VisitHistoryViewModel.Filter.ACTIVE)
        viewModel.setFilter(VisitHistoryViewModel.Filter.ALL)

        val state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.filter == VisitHistoryViewModel.Filter.ALL) { "Expected ALL filter after switching back" }
    }

    @Test
    fun `registrarSaida should update visit with saida timestamp and status`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit))
        coEvery { visitRepository.updateVisit(any()) } returns Result.success(
            activeVisit.copy(
                dataSaida = System.currentTimeMillis(),
                status = VisitStatus.SAIDA_REGISTRADA
            )
        )

        viewModel = VisitHistoryViewModel(visitRepository)

        viewModel.registrarSaida(activeVisit)

        coVerify {
            visitRepository.updateVisit(match { updatedVisit ->
                updatedVisit.id == activeVisit.id &&
                updatedVisit.status == VisitStatus.SAIDA_REGISTRADA &&
                updatedVisit.dataSaida != null
            })
        }
    }

    @Test
    fun `visitRepository flow updates should be reflected in uiState`() {
        val visitsFlow = MutableStateFlow(listOf(activeVisit))

        coEvery { visitRepository.observeAllVisits() } returns visitsFlow

        viewModel = VisitHistoryViewModel(visitRepository)

        // Initial state
        var state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state?.visits?.size == 1) { "Expected 1 visit initially" }

        // Simulate a new visit being added (reactive flow update)
        visitsFlow.value = listOf(activeVisit, completedVisit)

        state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state?.visits?.size == 2) { "Expected 2 visits after flow update" }
    }
}
