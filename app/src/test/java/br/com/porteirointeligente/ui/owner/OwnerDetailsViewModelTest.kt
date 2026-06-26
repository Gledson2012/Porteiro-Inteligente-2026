package br.com.porteirointeligente.ui.owner

import android.content.Context
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerDetailsViewModelTest {

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var ownerSelectionManager: OwnerSelectionManager

    @MockK
    private lateinit var context: Context

    private lateinit var viewModel: OwnerDetailsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testOwner = Owner(
        id = 1L,
        nome = "João Silva",
        endereco = "Rua das Flores, 100",
        apartamento = "42",
        telefone = "5511999998888",
        qrCodePayload = "teste_payload"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner))
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(1L)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 1L
        coEvery { ownerSelectionManager.selectOwner(any()) } just runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be Success when owner exists`() {
        viewModel = OwnerDetailsViewModel(ownerRepository, ownerSelectionManager, context)
        val state = viewModel.uiState.value
        assert(state is OwnerDetailsViewModel.OwnerDetailsUiState.Success) {
            "Expected Success but got $state"
        }
        if (state is OwnerDetailsViewModel.OwnerDetailsUiState.Success) {
            assert(state.owner.nome == "João Silva")
            assert(state.allOwners.size == 1)
        }
    }

    @Test
    fun `uiState should be Empty when no owners`() {
        coEvery { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(null)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns null

        viewModel = OwnerDetailsViewModel(ownerRepository, ownerSelectionManager, context)
        val state = viewModel.uiState.value
        assert(state is OwnerDetailsViewModel.OwnerDetailsUiState.Empty) {
            "Expected Empty but got $state"
        }
    }

    @Test
    fun `deleteOwner should call repository delete`() {
        coEvery { ownerRepository.getOwnerById(1L) } returns testOwner
        coEvery { ownerRepository.deleteOwner(any()) } returns Result.success(Unit)

        viewModel = OwnerDetailsViewModel(ownerRepository, ownerSelectionManager, context)
        viewModel.deleteOwner(1L)

        coVerify { ownerRepository.deleteOwner(testOwner) }
    }

    @Test
    fun `selecionarOwner should update selection`() {
        viewModel = OwnerDetailsViewModel(ownerRepository, ownerSelectionManager, context)
        viewModel.selecionarOwner(1L)
        coVerify { ownerSelectionManager.selectOwner(1L) }
    }
}
