package br.com.porteirointeligente.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import br.com.porteirointeligente.BuildConfig
import br.com.porteirointeligente.ui.theme.Amber
import br.com.porteirointeligente.ui.theme.Slate400
import br.com.porteirointeligente.util.AppTheme

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
    val ownerState by viewModel.owner.collectAsState()
    val allOwnersState by viewModel.allOwners.collectAsState()

    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showOwnerDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var tempMessage by remember { mutableStateOf("") }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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

            // Resident & Offline Mode Section
            item {
                SectionHeader(
                    icon = Icons.Default.Person,
                    title = "Morador & Modo Offline"
                )
            }

            item {
                SettingsCard {
                    if (allOwnersState.isEmpty()) {
                        SettingsClickItem(
                            icon = Icons.Default.Person,
                            iconBackground = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            iconTint = MaterialTheme.colorScheme.error,
                            title = "Nenhum Morador",
                            subtitle = "Cadastre um morador na aba Perfil",
                            onClick = {}
                        )
                    } else {
                        // Resident selection
                        SettingsClickItem(
                            icon = Icons.Default.Person,
                            iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            title = "Morador Selecionado",
                            subtitle = ownerState?.nome ?: "Nenhum selecionado",
                            onClick = {
                                if (allOwnersState.size > 1) {
                                    showOwnerDialog = true
                                }
                            }
                        )

                        Divider()

                        // Offline Mode Switch
                        SettingsSwitchItem(
                            icon = Icons.Default.NotificationsOff,
                            iconBackground = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            iconTint = MaterialTheme.colorScheme.error,
                            title = "Modo Ausente (Offline)",
                            subtitle = "Exibir mensagem de ausência ao ler QR Code",
                            checked = ownerState?.isOffline ?: false,
                            onCheckedChange = { isChecked ->
                                ownerState?.let { current ->
                                    viewModel.updateOfflineStatus(
                                        isOffline = isChecked,
                                        message = current.offlineMessage.ifBlank { "O morador está temporariamente indisponível." },
                                        durationMillis = if (isChecked) 3600000L else null // 1 hora
                                    )
                                }
                            }
                        )

                        if (ownerState?.isOffline == true) {
                            Divider()

                            // Offline Message
                            SettingsClickItem(
                                icon = Icons.AutoMirrored.Filled.Message,
                                iconBackground = MaterialTheme.colorScheme.primaryContainer,
                                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                title = "Mensagem de Ausência",
                                subtitle = ownerState?.offlineMessage?.ifBlank { "Nenhuma mensagem configurada" } ?: "Toque para configurar",
                                onClick = {
                                    tempMessage = ownerState?.offlineMessage ?: ""
                                    showMessageDialog = true
                                }
                            )

                            Divider()

                            // Offline Duration
                            val durationText = if (ownerState?.offlineUntil == null) {
                                "Até desativar"
                            } else {
                                val minutesLeft = ((ownerState!!.offlineUntil!! - System.currentTimeMillis()) / (60 * 1000)).toInt()
                                if (minutesLeft <= 0) {
                                    "Expirado (Toque para atualizar)"
                                } else if (minutesLeft < 60) {
                                    "Faltam $minutesLeft min"
                                } else {
                                    val hours = minutesLeft / 60
                                    val mins = minutesLeft % 60
                                    "Faltam ${hours}h ${mins}m"
                                }
                            }
                            SettingsClickItem(
                                icon = Icons.Default.Schedule,
                                iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
                                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                                title = "Duração da Ausência",
                                subtitle = durationText,
                                onClick = { showDurationDialog = true }
                            )
                        }
                    }
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
                        subtitle = BuildConfig.VERSION_NAME,
                        onClick = { showAboutDialog = true }
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

                    Divider()

                    SettingsClickItem(
                        icon = Icons.Default.Share,
                        iconBackground = Amber.copy(alpha = 0.15f),
                        iconTint = Amber,
                        title = "Compartilhar App",
                        subtitle = "Enviar link de download para amigos",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Porteiro Inteligente")
                                putExtra(Intent.EXTRA_TEXT, "Baixe o Porteiro Inteligente — app para gestão de portaria em condomínios: https://porteirointeligente.com")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar App"))
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
    // About dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Column {
                    Text(
                        "Porteiro Inteligente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Versão ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Melhorias desta versão:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    listOf(
                        "App 100% offline — sem servidor, sem depender de internet",
                        "Autenticação local — login e registro salvos diretamente no dispositivo",
                        "Backup e Restore — exporte dados em JSON e salve no Google Drive",
                        "Criptografia FBE — dados protegidos com criptografia de nível de sistema",
                        "App mais leve — remoção de Firebase e dependências de backend",
                        "Tema dinâmico — suporte a Material You / Monet"
                    ).forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                "•  ",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Desenvolvido por Gledson Crist Ribeiro dos Santos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                    Text(
                        "By Família Venâncio",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showOwnerDialog) {
        AlertDialog(
            onDismissRequest = { showOwnerDialog = false },
            title = {
                Text(
                    "Selecionar Morador",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allOwnersState.forEach { o ->
                        val isSelected = ownerState?.id == o.id
                        Card(
                            onClick = {
                                viewModel.selecionarOwnerParaConfig(o.id)
                                showOwnerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = o.nome,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "${o.nomeCondominio} - Ap. ${o.apartamento}",
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
                TextButton(onClick = { showOwnerDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = {
                Text(
                    "Mensagem de Ausência",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = tempMessage,
                    onValueChange = { tempMessage = it },
                    label = { Text("Mensagem") },
                    placeholder = { Text("Ex: Estou fora de casa...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ownerState?.let { current ->
                        viewModel.updateOfflineStatus(
                            isOffline = current.isOffline,
                            message = tempMessage,
                            durationMillis = current.offlineUntil?.let { it - System.currentTimeMillis() }
                        )
                    }
                    showMessageDialog = false
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDurationDialog) {
        val durations = listOf(
            "Indeterminado" to null,
            "15 minutos" to 15 * 60 * 1000L,
            "30 minutos" to 30 * 60 * 1000L,
            "1 hora" to 60 * 60 * 1000L,
            "2 horas" to 2 * 60 * 60 * 1000L,
            "4 horas" to 4 * 60 * 60 * 1000L,
            "8 horas" to 8 * 60 * 60 * 1000L,
            "24 horas" to 24 * 60 * 60 * 1000L
        )
        AlertDialog(
            onDismissRequest = { showDurationDialog = false },
            title = {
                Text(
                    "Duração do Modo Ausente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    durations.forEach { (label, duration) ->
                        val isSelected = if (duration == null) {
                            ownerState?.offlineUntil == null
                        } else {
                            ownerState?.offlineUntil != null && Math.abs((ownerState!!.offlineUntil!! - System.currentTimeMillis()) - duration) < 60000L
                        }
                        Card(
                            onClick = {
                                ownerState?.let { current ->
                                    viewModel.updateOfflineStatus(
                                        isOffline = current.isOffline,
                                        message = current.offlineMessage,
                                        durationMillis = duration
                                    )
                                }
                                showDurationDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
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
                TextButton(onClick = { showDurationDialog = false }) {
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
            .clickable { onClick() }
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
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}
