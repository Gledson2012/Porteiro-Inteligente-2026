package br.com.porteirointeligente.ui.visit

import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import br.com.porteirointeligente.util.OwnerSelectionManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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

    @MockK
    private lateinit var ownerSelectionManager: OwnerSelectionManager

    private lateinit var viewModel: VisitHistoryViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val activeVisit = Visit(
        id = 1L,
        ownerId = 1L,
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
        ownerId = 1L,
        nome = "Ana",
        documento = "RG456",
        apartamento = "202",
        telefone = "11988888888",
        motivo = "Visita",
        dataEntrada = System.currentTimeMillis() - 3600000,
        dataSaida = System.currentTimeMillis(),
        status = VisitStatus.SAIDA_REGISTRADA
    )

    private val visitWithPlate = Visit(
        id = 3L,
        ownerId = 1L,
        nome = "Marcos",
        documento = "RG789",
        apartamento = "303",
        telefone = "11977777777",
        motivo = "Serviço",
        placa = "BRA2E19",
        dataEntrada = System.currentTimeMillis(),
        status = VisitStatus.ENTRADA_REGISTRADA
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(1L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        // flow {} (empty lambda) never emits, keeping ViewModel in Loading state
        coEvery { visitRepository.observeAllVisits() } returns flow { }

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)

        assert(viewModel.uiState.value is VisitHistoryUIState.Loading) {
            "Expected Loading state initially, got ${viewModel.uiState.value::class.simpleName}"
        }
    }

    @Test
    fun `loadVisits with ALL filter should emit Success with all visits`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit, completedVisit))

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)

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

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)

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

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)

        viewModel.setFilter(VisitHistoryViewModel.Filter.ACTIVE)
        viewModel.setFilter(VisitHistoryViewModel.Filter.ALL)

        val state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.filter == VisitHistoryViewModel.Filter.ALL) { "Expected ALL filter after switching back" }
    }

    @Test
    fun `setFilter to COMPLETED should filter visits by SAIDA_REGISTRADA`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit, completedVisit))
        coEvery { visitRepository.observeVisitsByStatus(VisitStatus.SAIDA_REGISTRADA) } returns flowOf(listOf(completedVisit))

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)
        viewModel.setFilter(VisitHistoryViewModel.Filter.COMPLETED)

        val state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.filter == VisitHistoryViewModel.Filter.COMPLETED) { "Expected COMPLETED filter" }
        assert(state?.visits?.size == 1) { "Expected 1 visit after filtering, got ${state?.visits?.size}" }
        assert(state?.visits?.first()?.status == VisitStatus.SAIDA_REGISTRADA) { "Expected completed visit only" }
    }

    @Test
    fun `setFilter to WITH_PLATE should filter visits having non-blank placa`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit, completedVisit, visitWithPlate))

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)
        viewModel.setFilter(VisitHistoryViewModel.Filter.WITH_PLATE)

        val state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.filter == VisitHistoryViewModel.Filter.WITH_PLATE) { "Expected WITH_PLATE filter" }
        assert(state?.visits?.size == 1) { "Expected 1 visit with plate, got ${state?.visits?.size}" }
        assert(state?.visits?.first()?.placa == "BRA2E19") { "Expected visit with plate BRA2E19" }
    }

    @Test
    fun `registrarSaida should update visit with saida timestamp and status`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit))
        coEvery { visitRepository.updateVisit(any()) } just runs

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)

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
    fun `clearAllVisits should clear only the selected owner's visits`() {
        coEvery { visitRepository.clearByOwnerId(1L) } just runs
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit))

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)
        viewModel.clearAllVisits(ownerId = 1L)

        coVerify(exactly = 1) { visitRepository.clearByOwnerId(1L) }
        coVerify(exactly = 0) { visitRepository.clearAll() }
    }

    @Test
    fun `clearAllVisits should refuse to clear when no owner is selected`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit))

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)
        viewModel.clearAllVisits(ownerId = null)

        coVerify(exactly = 0) { visitRepository.clearByOwnerId(any()) }
        assert(viewModel.actionError.value == "Selecione um morador antes de limpar o histórico.")
    }

    @Test
    fun `deleteVisit should expose repository failures to the UI`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(activeVisit))
        coEvery { visitRepository.deleteVisit(activeVisit) } throws IllegalStateException("Falha ao excluir")

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)
        viewModel.deleteVisit(activeVisit)

        assert(viewModel.actionError.value == "Falha ao excluir")
    }

    @Test
    fun `visitRepository flow updates should be reflected in uiState`() {
        val visitsFlow = MutableStateFlow(listOf(activeVisit))

        coEvery { visitRepository.observeAllVisits() } returns visitsFlow

        viewModel = VisitHistoryViewModel(visitRepository, ownerSelectionManager)

        // Initial state
        var state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state?.visits?.size == 1) { "Expected 1 visit initially" }

        // Simulate a new visit being added (reactive flow update)
        visitsFlow.value = listOf(activeVisit, completedVisit)

        state = viewModel.uiState.value as? VisitHistoryUIState.Success
        assert(state?.visits?.size == 2) { "Expected 2 visits after flow update" }
    }
}
