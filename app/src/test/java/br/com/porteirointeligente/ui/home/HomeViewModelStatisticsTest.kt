package br.com.porteirointeligente.ui.home

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import br.com.porteirointeligente.util.OwnerSelectionManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
class HomeViewModelStatisticsTest {

    @MockK
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var ownerSelectionManager: OwnerSelectionManager

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val today = System.currentTimeMillis()
    private val yesterday = today - 86400000L // 1 dia atrás

    private val visitsToday = listOf(
        Visit(1, "Carlos", "RG1", "42", "11999999999", "Entrega", today, status = VisitStatus.ENTRADA_REGISTRADA),
        Visit(2, "Maria", "RG2", "15", "11999999998", "Visita", today, status = VisitStatus.ENTRADA_REGISTRADA),
        Visit(3, "Carlos", "RG1", "42", "11999999999", "Retorno", today, null, VisitStatus.SAIDA_REGISTRADA),
        Visit(4, "João", "RG3", "10", "11999999997", "Manutenção", yesterday, status = VisitStatus.ENTRADA_REGISTRADA)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(null)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns null
        coEvery { visitRepository.observeAllVisits() } returns flowOf(visitsToday)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `statsTotalHoje should count only today visits`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        // 3 visitas hoje (Carlos, Maria, Carlos saída)
        assert(viewModel.statsTotalHoje.value == 3) {
            "Expected 3 but got ${viewModel.statsTotalHoje.value}"
        }
    }

    @Test
    fun `statsEntradasHoje should count active entries today`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        // 2 entradas ativas hoje (Carlos e Maria estão com ENTRADA_REGISTRADA)
        assert(viewModel.statsEntradasHoje.value == 2) {
            "Expected 2 but got ${viewModel.statsEntradasHoje.value}"
        }
    }

    @Test
    fun `statsVisitantesUnicos should count unique visitors today`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        // 2 visitantes únicos hoje (Carlos e Maria)
        assert(viewModel.statsVisitantesUnicos.value == 2) {
            "Expected 2 but got ${viewModel.statsVisitantesUnicos.value}"
        }
    }

    @Test
    fun `stats should be zero when no visits`() {
        coEvery { visitRepository.observeAllVisits() } returns flowOf(emptyList())

        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        assert(viewModel.statsTotalHoje.value == 0)
        assert(viewModel.statsEntradasHoje.value == 0)
        assert(viewModel.statsVisitantesUnicos.value == 0)
    }
}
