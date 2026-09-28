package br.com.porteirointeligente.ui.owner

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerManagementViewModelTest {

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var ownerSelectionManager: OwnerSelectionManager

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testOwner = Owner(
        id = 10L,
        nome = "Maria Santos",
        endereco = "Av. Brasil, 500",
        cep = "01000000",
        apartamento = "302",
        telefone = "11988887777",
        qrCodePayload = "v2.10.fake"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner))
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 10L
        coEvery { ownerSelectionManager.clearSelection() } just runs
        coEvery { ownerRepository.deleteOwner(any()) } just runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadOwners loads owners into Success state`() {
        val viewModel = OwnerManagementViewModel(ownerRepository, ownerSelectionManager)
        assertTrue(viewModel.uiState.value is OwnerUIState.Success)
        val state = viewModel.uiState.value as OwnerUIState.Success
        assertEquals(1, state.owners.size)
        assertEquals("Maria Santos", state.owners[0].nome)
    }

    @Test
    fun `deleteOwner deletes owner and clears selection if selected`() {
        val viewModel = OwnerManagementViewModel(ownerRepository, ownerSelectionManager)
        viewModel.deleteOwner(testOwner)

        coVerify(exactly = 1) { ownerRepository.deleteOwner(testOwner) }
        coVerify(exactly = 1) { ownerSelectionManager.clearSelection() }
    }

    @Test
    fun `deleteOwner does not clear selection if different owner is selected`() {
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 999L
        val viewModel = OwnerManagementViewModel(ownerRepository, ownerSelectionManager)
        viewModel.deleteOwner(testOwner)

        coVerify(exactly = 1) { ownerRepository.deleteOwner(testOwner) }
        coVerify(exactly = 0) { ownerSelectionManager.clearSelection() }
    }
}
