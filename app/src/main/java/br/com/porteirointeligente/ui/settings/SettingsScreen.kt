package br.com.porteirointeligente.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.util.AppTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val owner by viewModel.owner.collectAsState()
    val themeState by viewModel.themeState.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val restoreState by viewModel.restoreState.collectAsState()

    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.restoreBackup(it) }
        }
    )

    LaunchedEffect(backupState) {
        if (backupState is SettingsViewModel.BackupState.Success) {
            Toast.makeText(context, "Backup gerado com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.resetBackupState()
        } else if (backupState is SettingsViewModel.BackupState.Error) {
            Toast.makeText(context, (backupState as SettingsViewModel.BackupState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetBackupState()
        }
    }

    LaunchedEffect(restoreState) {
        if (restoreState is SettingsViewModel.RestoreState.Success) {
            Toast.makeText(context, "Dados restaurados com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.resetRestoreState()
        } else if (restoreState is SettingsViewModel.RestoreState.Error) {
            Toast.makeText(context, (restoreState as SettingsViewModel.RestoreState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetRestoreState()
        }
    }
    
    var isOffline by remember { mutableStateOf(false) }
    var offlineMessage by remember { mutableStateOf("") }
    var selectedDurationIndex by remember { mutableIntStateOf(3) } // Default: Forever

    val durations = listOf("2h", "8h", "1 semana", "Sempre")
    val durationMillis = listOf(
        TimeUnit.HOURS.toMillis(2),
        TimeUnit.HOURS.toMillis(8),
        TimeUnit.DAYS.toMillis(7),
        null
    )

    LaunchedEffect(owner) {
        owner?.let {
            isOffline = it.isOffline
            offlineMessage = it.offlineMessage
            selectedDurationIndex = if (it.offlineUntil == null) 3 else 0 // Simplificado
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurações") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(title = "Aparência", icon = Icons.Default.Brightness6) {
                Text("Tema do Aplicativo", style = MaterialTheme.typography.bodyLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf("Claro", "Escuro", "Sistema")
                    val themes = listOf(AppTheme.LIGHT, AppTheme.DARK, AppTheme.SYSTEM)
                    
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            onClick = { viewModel.setTheme(themes[index]) },
                            selected = themeState == themes[index]
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            SettingsSection(title = "Modo Offline", icon = Icons.Default.CloudOff) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ativar Modo Offline", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Visitantes verão um aviso ao ler seu QR Code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Switch(checked = isOffline, onCheckedChange = { isOffline = it })
                }

                if (isOffline) {
                    OutlinedTextField(
                        value = offlineMessage,
                        onValueChange = { offlineMessage = it },
                        label = { Text("Mensagem de ausência") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: Estou em reunião, volto logo.") }
                    )

                    Text("Duração", style = MaterialTheme.typography.bodyLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        durations.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = durations.size),
                                onClick = { selectedDurationIndex = index },
                                selected = index == selectedDurationIndex
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            SettingsSection(title = "Dados e Segurança", icon = Icons.Default.Backup) {
                Text(
                    "Proteja suas informações gerando um arquivo de backup com seu perfil e histórico de visitas ou restaure seus dados a partir de um backup.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
                    onClick = { viewModel.performBackup() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("FAZER BACKUP DOS DADOS")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("application/json") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("RESTAURAR BACKUP (IMPORTAR JSON)")
                }
            }

            Button(
                onClick = {
                    viewModel.updateOfflineStatus(
                        isOffline,
                        offlineMessage,
                        durationMillis[selectedDurationIndex]
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SALVAR ALTERAÇÕES")
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        content()
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
