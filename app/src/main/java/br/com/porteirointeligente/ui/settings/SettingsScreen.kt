package br.com.porteirointeligente.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.ui.theme.Amber
import br.com.porteirointeligente.ui.theme.Slate400
import br.com.porteirointeligente.util.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeState by viewModel.themeState.collectAsState()
    val dynamicColorState by viewModel.dynamicColorState.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val restoreState by viewModel.restoreState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val firebaseSyncState by viewModel.firebaseSyncState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showThemeDialog by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.restoreBackup(uri)
        }
    }

    LaunchedEffect(restoreState) {
        if (restoreState is SettingsViewModel.RestoreState.Success) {
            viewModel.resetRestoreState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajustes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance section
            item {
                SectionHeader(
                    icon = Icons.Default.ColorLens,
                    title = "Aparência"
                )
            }

            item {
                SettingsCard {
                    // Theme
                    SettingsClickItem(
                        icon = Icons.Default.Palette,
                        iconBackground = Amber.copy(alpha = 0.15f),
                        iconTint = Amber,
                        title = "Tema",
                        subtitle = when (themeState) {
                            AppTheme.LIGHT -> "Claro"
                            AppTheme.DARK -> "Escuro"
                            AppTheme.SYSTEM -> "Sistema"
                        },
                        onClick = { showThemeDialog = true }
                    )

                    Divider()

                    // Dynamic Color
                    SettingsSwitchItem(
                        icon = Icons.Default.BrightnessMedium,
                        iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        title = "Cores dinâmicas",
                        subtitle = "Usar paleta de cores do sistema",
                        checked = dynamicColorState,
                        onCheckedChange = { viewModel.setDynamicColor(it) }
                    )
                }
            }

            // Data section
            item {
                SectionHeader(
                    icon = Icons.Default.CloudUpload,
                    title = "Dados"
                )
            }

            item {
                SettingsCard {
                    // Backup
                    BackupItem(
                        state = backupState,
                        onBackup = { viewModel.performBackup() },
                        onReset = { viewModel.resetBackupState() }
                    )

                    Divider()

                    // Restore
                    RestoreItem(
                        state = restoreState,
                        onRestore = {
                            restoreLauncher.launch(arrayOf("application/json"))
                        },
                        onReset = { viewModel.resetRestoreState() }
                    )
                }
            }

            // Sync section
            item {
                SectionHeader(
                    icon = Icons.Default.CloudSync,
                    title = "Sincronização"
                )
            }

            item {
                SettingsCard {
                    SyncItem(
                        state = syncState,
                        title = "Servidor REST",
                        icon = Icons.Default.CloudUpload,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        onSync = { viewModel.syncWithRest() },
                        onReset = { viewModel.resetSyncStates() }
                    )

                    Divider()

                    SyncItem(
                        state = firebaseSyncState,
                        title = "Firebase",
                        icon = Icons.Default.CloudSync,
                        iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        onSync = { viewModel.syncWithFirebase() },
                        onReset = { viewModel.resetSyncStates() }
                    )
                }
            }

            // About section
            item {
                SectionHeader(
                    icon = Icons.Default.Info,
                    title = "Sobre"
                )
            }

            item {
                SettingsCard {
                    SettingsClickItem(
                        icon = Icons.Default.Info,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        title = "Versão",
                        subtitle = "0.1.0",
                        onClick = { }
                    )

                    Divider()

                    SettingsClickItem(
                        icon = Icons.Default.Link,
                        iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        title = "Site",
                        subtitle = "porteirointeligente.com",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://porteirointeligente.com"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item { br.com.porteirointeligente.ui.components.AppSignature() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Theme dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Escolher Tema",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = themeState == theme
                        Card(
                            onClick = {
                                viewModel.setTheme(theme)
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = if (isSelected) null
                            else androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (theme) {
                                        AppTheme.SYSTEM -> Icons.Outlined.BrightnessMedium
                                        AppTheme.LIGHT -> Icons.Outlined.Palette
                                        AppTheme.DARK -> Icons.Outlined.DarkMode
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (theme) {
                                            AppTheme.SYSTEM -> "Sistema"
                                            AppTheme.LIGHT -> "Claro"
                                            AppTheme.DARK -> "Escuro"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Text(
                                        text = when (theme) {
                                            AppTheme.SYSTEM -> "Acompanha o tema do dispositivo"
                                            AppTheme.LIGHT -> "Tema claro"
                                            AppTheme.DARK -> "Tema escuro"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate400
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun BackupItem(
    state: SettingsViewModel.BackupState,
    onBackup: () -> Unit,
    onReset: () -> Unit
) {
    when (state) {
        is SettingsViewModel.BackupState.Loading -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Gerando backup...")
            }
        }
        is SettingsViewModel.BackupState.Success -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Backup gerado com sucesso!")
            }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                onReset()
            }
        }
        is SettingsViewModel.BackupState.Error -> {
            SettingsClickItem(
                icon = Icons.Default.FileDownload,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Backup",
                subtitle = state.message,
                onClick = onBackup
            )
        }
        is SettingsViewModel.BackupState.Idle -> {
            SettingsClickItem(
                icon = Icons.Default.FileDownload,
                iconBackground = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                title = "Fazer Backup",
                subtitle = "Exportar dados do aplicativo",
                onClick = onBackup
            )
        }
    }
}

@Composable
private fun RestoreItem(
    state: SettingsViewModel.RestoreState,
    onRestore: () -> Unit,
    onReset: () -> Unit
) {
    when (state) {
        is SettingsViewModel.RestoreState.Loading -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Restaurando backup...")
            }
        }
        is SettingsViewModel.RestoreState.Success -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dados restaurados!")
            }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                onReset()
            }
        }
        is SettingsViewModel.RestoreState.Error -> {
            SettingsClickItem(
                icon = Icons.Default.FileUpload,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Restaurar",
                subtitle = state.message,
                onClick = onRestore
            )
        }
        is SettingsViewModel.RestoreState.Idle -> {
            SettingsClickItem(
                icon = Icons.Default.FileUpload,
                iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                title = "Restaurar Backup",
                subtitle = "Importar dados de um arquivo",
                onClick = onRestore
            )
        }
    }
}

@Composable
private fun SyncItem(
    state: SettingsViewModel.SyncState,
    title: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    onSync: () -> Unit,
    onReset: () -> Unit
) {
    when (state) {
        is SettingsViewModel.SyncState.Syncing -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Sincronizando $title...")
            }
        }
        is SettingsViewModel.SyncState.Success -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(state.message)
            }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                onReset()
            }
        }
        is SettingsViewModel.SyncState.Error -> {
            SettingsClickItem(
                icon = icon,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Sincronizar $title",
                subtitle = state.message,
                onClick = onSync
            )
        }
        is SettingsViewModel.SyncState.Idle -> {
            SettingsClickItem(
                icon = icon,
                iconBackground = iconBackground,
                iconTint = iconTint,
                title = "Sincronizar $title",
                subtitle = "Enviar dados para $title",
                onClick = onSync
            )
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}
