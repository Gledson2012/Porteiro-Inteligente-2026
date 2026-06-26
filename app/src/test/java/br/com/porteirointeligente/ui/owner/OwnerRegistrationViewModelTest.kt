package br.com.porteirointeligente.ui.owner

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
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
class OwnerRegistrationViewModelTest {

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    private lateinit var viewModel: OwnerRegistrationViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testOwner = Owner(
        id = 1L,
        nome = "João Silva",
        nomeCondominio = "Residencial Verde",
        endereco = "Av. Central, 500",
        cep = "20040002",
        apartamento = "201",
        telefone = "11988887777",
        photoUri = null,
        qrCodePayload = "https://porteiro-inteligente.web.app/scan/1_hash"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        coEvery { ownerRepository.getOwnerById(any()) } returns null
        coEvery { ownerRepository.insertOwner(any()) } returns testOwner
        coEvery { ownerRepository.updateOwner(any()) } returns Unit
        
        viewModel = OwnerRegistrationViewModel(ownerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadOwner should return null when no owner found`() = runTest(testDispatcher) {
        val result = viewModel.loadOwner(999L)
        assert(result == null) { "Expected null for non-existent owner" }
    }

    @Test
    fun `loadOwner should return owner when found`() = runTest(testDispatcher) {
        coEvery { ownerRepository.getOwnerById(1L) } returns testOwner
        val result = viewModel.loadOwner(1L)
        assert(result != null) { "Expected owner to be found" }
        assert(result?.nome == "João Silva") { "Expected name to match" }
    }

    @Test
    fun `registerOwner with blank nome should set Error state`() = runTest(testDispatcher) {
        viewModel.registerOwner(
            id = 0L,
            nome = "",
            nomeCondominio = "",
            endereco = "Rua Teste",
            cep = "01310000",
            apartamento = "",
            telefone = "11999999999",
            photoUri = null
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is OwnerRegistrationUIState.Error)
        assert((state as OwnerRegistrationUIState.Error).message == "Nome, endereço e telefone são obrigatórios.")
    }

    @Test
    fun `registerOwner with blank endereco should set Error state`() = runTest(testDispatcher) {
        viewModel.registerOwner(
            id = 0L,
            nome = "João Silva",
            nomeCondominio = "",
            endereco = "",
            cep = "01310000",
            apartamento = "",
            telefone = "11999999999",
            photoUri = null
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is OwnerRegistrationUIState.Error)
        assert((state as OwnerRegistrationUIState.Error).message == "Nome, endereço e telefone são obrigatórios.")
    }

    @Test
    fun `registerOwner with blank telefone should set Error state`() = runTest(testDispatcher) {
        viewModel.registerOwner(
            id = 0L,
            nome = "João Silva",
            nomeCondominio = "",
            endereco = "Rua Teste",
            cep = "01310000",
            apartamento = "",
            telefone = "",
            photoUri = null
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is OwnerRegistrationUIState.Error)
        assert((state as OwnerRegistrationUIState.Error).message == "Nome, endereço e telefone são obrigatórios.")
    }

    @Test
    fun `registerOwner with valid data should insert and set Success state`() = runTest(testDispatcher) {
        viewModel.registerOwner(
            id = 0L,
            nome = "Carlos Souza",
            nomeCondominio = "Residencial Verde",
            endereco = "Av. Central, 500",
            cep = "20040002",
            apartamento = "201",
            telefone = "11988887777",
            photoUri = null
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is OwnerRegistrationUIState.Success)
        coVerify { ownerRepository.insertOwner(any()) }
    }

    @Test
    fun `registerOwner for editing should update owner and set Success state`() = runTest(testDispatcher) {
        coEvery { ownerRepository.getOwnerById(5L) } returns testOwner

        viewModel.registerOwner(
            id = 5L,
            nome = "João Silva Atualizado",
            nomeCondominio = "Edifício Central",
            endereco = "Rua Nova, 200",
            cep = "22222000",
            apartamento = "501",
            telefone = "5511988887777",
            photoUri = null
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state is OwnerRegistrationUIState.Success)
        coVerify { ownerRepository.updateOwner(any()) }
    }
}
