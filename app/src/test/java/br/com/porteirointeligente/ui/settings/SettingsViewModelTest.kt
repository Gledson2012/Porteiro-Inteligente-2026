package br.com.porteirointeligente.ui.settings

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.AppTheme
import br.com.porteirointeligente.util.BackupManager
import br.com.porteirointeligente.util.OwnerSelectionManager
import br.com.porteirointeligente.util.ThemeManager
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var themeManager: ThemeManager

    @MockK
    private lateinit var backupManager: BackupManager

    @MockK
    private lateinit var ownerSelectionManager: OwnerSelectionManager

    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        every { themeManager.themeFlow } returns flowOf(AppTheme.SYSTEM)
        every { themeManager.dynamicColorFlow } returns flowOf(false)
        coEvery { themeManager.setTheme(any()) } just runs
        coEvery { themeManager.setDynamicColor(any()) } just runs

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(emptyList())
        every { ownerSelectionManager.selectedOwnerId } returns flowOf(null)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns null
        coEvery { backupManager.generateBackupAndShare() } just runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `themeState should default to SYSTEM`() {
        viewModel = SettingsViewModel(ownerRepository, themeManager, backupManager, ownerSelectionManager)
        assert(viewModel.themeState.value == AppTheme.SYSTEM) {
            "Expected SYSTEM but got ${viewModel.themeState.value}"
        }
    }

    @Test
    fun `dynamicColorState should default to false`() {
        viewModel = SettingsViewModel(ownerRepository, themeManager, backupManager, ownerSelectionManager)
        assert(!viewModel.dynamicColorState.value) {
            "Expected false but got ${viewModel.dynamicColorState.value}"
        }
    }

    @Test
    fun `setTheme should call themeManager setTheme`() {
        viewModel = SettingsViewModel(ownerRepository, themeManager, backupManager, ownerSelectionManager)
        viewModel.setTheme(AppTheme.DARK)
        coVerify { themeManager.setTheme(AppTheme.DARK) }
    }

    @Test
    fun `setDynamicColor should call themeManager setDynamicColor`() {
        viewModel = SettingsViewModel(ownerRepository, themeManager, backupManager, ownerSelectionManager)
        viewModel.setDynamicColor(true)
        coVerify { themeManager.setDynamicColor(true) }
    }

    @Test
    fun `backupState should be Idle initially`() {
        viewModel = SettingsViewModel(ownerRepository, themeManager, backupManager, ownerSelectionManager)
        assert(viewModel.backupState.value is SettingsViewModel.BackupState.Idle)
    }

    @Test
    fun `performBackup should call backupManager`() {
        viewModel = SettingsViewModel(ownerRepository, themeManager, backupManager, ownerSelectionManager)
        viewModel.performBackup()
        coVerify { backupManager.generateBackupAndShare() }
    }

    @Test
    fun `updateOfflineStatus should update owner`() {
        val testOwner = Owner(
            id = 1L,
            nome = "Teste",
            endereco = "Rua 1",
            apartamento = "42",
            telefone = "5511999998888",
            qrCodePayload = "teste"
        )

        coEvery { ownerRepository.observeAllOwners() } returns flowOf(listOf(testOwner))
        coEvery { ownerRepository.updateOwner(any()) } returns Result.success(testOwner)
        coEvery { ownerSelectionManager.getSelectedOwnerId() } returns 1L

        viewModel = SettingsViewModel(ownerRepository, themeManager, backupManager, ownerSelectionManager)
        viewModel.updateOfflineStatus(true, "Estou ausente", 3600000L)

        coVerify { ownerRepository.updateOwner(match { it.isOffline && it.offlineMessage == "Estou ausente" }) }
    }
}
