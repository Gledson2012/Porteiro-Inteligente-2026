package br.com.porteirointeligente.ui.home

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
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
class HomeViewModelTest {

    @MockK
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var ownerSelectionManager: OwnerSelectionManager

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(null)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns null
        coEvery { visitRepository.observeAllVisits() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isLoading should be false after initialization`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        assert(!viewModel.isLoading.value) { "isLoading should be false after init" }
    }

    @Test
    fun `configurarIdentificacao should update condominio and apartamento`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        viewModel.configurarIdentificacao("Condomínio Teste", "Apto 42")

        assert(viewModel.condominio.value == "Condomínio Teste")
        assert(viewModel.apartamento.value == "Apto 42")
    }

    @Test
    fun `visitasRecentes should return empty list by default`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        assert(viewModel.visitasRecentes.value.isEmpty())
    }

    @Test
    fun `moradorCadastrado should be null when no owners`() {
        viewModel = HomeViewModel(visitRepository, ownerRepository, ownerSelectionManager)
        assert(viewModel.moradorCadastrado.value == null)
    }
}
