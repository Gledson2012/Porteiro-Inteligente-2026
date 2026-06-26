package br.com.porteirointeligente.ui.visit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitRegistrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: VisitRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var nome by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    
    var nomeError by remember { mutableStateOf<String?>(null) }
    var apartamentoError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is VisitRegistrationUIState.Success -> {
                onNavigateBack()
            }
            is VisitRegistrationUIState.Error -> {
                if (nome.isBlank()) nomeError = "Campo obrigatório"
                if (apartamento.isBlank()) apartamentoError = "Campo obrigatório"
            }
            else -> {}
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Visita") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { 
                    nome = it
                    nomeError = null
                },
                label = { Text("Nome Completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                isError = nomeError != null,
                supportingText = { nomeError?.let { Text(it) } },
                colors = textFieldColors
            )

            OutlinedTextField(
                value = documento,
                onValueChange = { documento = it },
                label = { Text("Documento (RG/CPF)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            OutlinedTextField(
                value = apartamento,
                onValueChange = { 
                    apartamento = it
                    apartamentoError = null
                },
                label = { Text("Apartamento/Unidade") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                isError = apartamentoError != null,
                supportingText = { apartamentoError?.let { Text(it) } },
                colors = textFieldColors
            )

            OutlinedTextField(
                value = telefone,
                onValueChange = { input ->
                    val clean = input.replace(Regex("[^0-9]"), "")
                    if (clean.length <= 11) {
                        telefone = when {
                            clean.length > 7 -> {
                                val ddd = clean.take(2)
                                val firstPart = clean.substring(2, clean.length - 4)
                                val secondPart = clean.substring(clean.length - 4)
                                "($ddd) $firstPart-$secondPart"
                            }
                            clean.length > 2 -> {
                                val ddd = clean.take(2)
                                val firstPart = clean.substring(2)
                                "($ddd) $firstPart"
                            }
                            else -> clean
                        }
                    }
                },
                label = { Text("Telefone") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = textFieldColors
            )

            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text("Motivo da Visita") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = textFieldColors
            )

            Button(
                onClick = {
                    viewModel.registrarVisita(nome, documento, apartamento, telefone, motivo)
                },
                enabled = uiState !is VisitRegistrationUIState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (uiState is VisitRegistrationUIState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("REGISTRAR ENTRADA")
                }
            }
        }
    }
}
