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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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

    private val testOwner = Owner(
        id = 1L,
        nome = "João Silva",
        endereco = "Rua Teste, 123",
        cep = "01310123",
        apartamento = "101",
        telefone = "5511999999999",
        qrCodePayload = "https://porteiro-inteligente.web.app/scan/1_hash"
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

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner))
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(1L)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 1L
        coEvery { visitRepository.observeAllVisits() } returns flowOf(listOf(testVisit))
        coEvery { ownerSelectionManager.selectOwner(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel initialization should load owners and visits and emit Success`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        
        val state = viewModel.uiState.value
        assert(state is HomeUIState.Success)
        val successState = state as HomeUIState.Success
        assert(successState.allOwners.size == 1)
        assert(successState.selectedOwner == testOwner)
        assert(successState.recentVisits.size == 1)
    }

    @Test
    fun `selecionarMorador should update selected owner and reload data`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        viewModel.selecionarMorador(2L)

        coVerify { ownerSelectionManager.selectOwner(2L) }
    }
}
