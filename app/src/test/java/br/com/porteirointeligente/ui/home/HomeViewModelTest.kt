package br.com.porteirointeligente.ui.home

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @MockK
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var ownerSelectionManager: OwnerSelectionManager

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testOwner1 = Owner(
        id = 1L,
        nome = "João Silva",
        endereco = "Rua Teste, 123",
        cep = "01310123",
        apartamento = "101",
        telefone = "5511999999999",
        qrCodePayload = "https://porteiro-inteligente.web.app/scan/1_hash"
    )

    private val testOwner2 = Owner(
        id = 2L,
        nome = "Maria Souza",
        endereco = "Rua Teste, 456",
        cep = "01310123",
        apartamento = "102",
        telefone = "5511988888888",
        qrCodePayload = "https://porteiro-inteligente.web.app/scan/2_hash"
    )

    private val testVisit = Visit(
        id = 1L,
        nome = "Carlos",
        documento = "RG123",
        apartamento = "101",
        telefone = "11999999999",
        motivo = "Entrega",
        dataEntrada = System.currentTimeMillis(),
        status = VisitStatus.ENTRADA_REGISTRADA
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
    fun `viewModel initialization with selected owner should emit Success`() {
        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner1))
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(1L)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 1L
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(testVisit))

        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)

        val state = viewModel.uiState.value
        assert(state is HomeUIState.Success)
        val successState = state as HomeUIState.Success
        assert(successState.allOwners.size == 1) { "Expected 1 owner" }
        assert(successState.selectedOwner?.id == 1L) { "Expected selected owner id 1" }
        assert(successState.recentVisits.size == 1) { "Expected 1 visit" }
    }

    @Test
    fun `viewModel initialization without selected owner should auto-select first owner`() {
        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner1, testOwner2))
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(null)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns null
        coEvery { ownerSelectionManager.selectOwner(any()) } just runs
        coEvery { visitRepository.observeAllVisits() } returns flowOf(emptyList())

        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)

        // Auto-selection should trigger selectOwner for the first owner
        coVerify { ownerSelectionManager.selectOwner(1L) }
    }

    @Test
    fun `viewModel initialization with no owners should emit Success with empty lists`() {
        coEvery { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(null)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns null
        coEvery { visitRepository.observeAllVisits() } returns flowOf(emptyList())

        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)

        val state = viewModel.uiState.value
        assert(state is HomeUIState.Success)
        val successState = state as HomeUIState.Success
        assert(successState.allOwners.isEmpty()) { "Expected empty owners" }
        assert(successState.selectedOwner == null) { "Expected null selected owner" }
        assert(successState.recentVisits.isEmpty()) { "Expected empty visits" }
    }

    @Test
    fun `viewModel should respect selected owner when there are multiple owners`() {
        val selectedOwnerIdFlow = MutableStateFlow<Long?>(2L)

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner1, testOwner2))
        every { ownerSelectionManager.selectedOwnerId } returns selectedOwnerIdFlow
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 2L
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(testVisit))

        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)

        // Let the auto-selection in init complete
        val state = viewModel.uiState.value as? HomeUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.selectedOwner?.id == 2L) { "Expected selected owner id 2, got ${state?.selectedOwner?.id}" }
    }

    @Test
    fun `viewModel should update selectedOwnerId flow via selecionarMorador`() {
        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner1, testOwner2))
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(1L)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 1L
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(testVisit))
        coEvery { ownerSelectionManager.selectOwner(any()) } just runs

        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)

        viewModel.selecionarMorador(2L)
        coVerify { ownerSelectionManager.selectOwner(2L) }
    }

    @Test
    fun `viewModel should show only recent 5 visits`() {
        val manyVisits = (1..10).map { i ->
            Visit(
                id = i.toLong(),
                nome = "Visitante $i",
                documento = "RG$i",
                apartamento = "101",
                telefone = "1199999999$i",
                motivo = "Motivo $i",
                dataEntrada = System.currentTimeMillis(),
                status = VisitStatus.ENTRADA_REGISTRADA
            )
        }

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner1))
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(1L)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 1L
        coEvery { visitRepository.observeAllVisits() } returns flowOf(manyVisits)

        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)

        val state = viewModel.uiState.value as? HomeUIState.Success
        assert(state != null) { "Expected Success state" }
        assert(state?.recentVisits?.size == 5) { "Expected only 5 recent visits, got ${state?.recentVisits?.size}" }
    }
}
