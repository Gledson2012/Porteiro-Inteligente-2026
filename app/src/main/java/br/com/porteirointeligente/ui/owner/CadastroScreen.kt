package br.com.porteirointeligente.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.porteirointeligente.ui.components.AppSignature
import br.com.porteirointeligente.ui.theme.GradientNeon
import br.com.porteirointeligente.util.FirebaseSyncService
import br.com.porteirointeligente.util.ViaCepClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Tela de Cadastro do Proprietário.
 *
 * Suporta tanto cadastro novo (ownerId = 0L) quanto edição (ownerId > 0L).
 * - Formulário: Nome Completo, Endereço, CEP, Celular
 * - Máscaras visuais em tempo real para CEP (00000-000) e Celular ((00) 00000-0000)
 * - Botão "GERAR QR CODE" que valida, salva (simulando Firebase) e navega
 */
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import br.com.porteirointeligente.ui.theme.GradientAmber
import br.com.porteirointeligente.ui.theme.Slate400
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroScreen(
    onNavigateBack: () -> Unit,
    onCadastroConcluido: (Long) -> Unit = {},
    ownerId: Long = 0L,
    viewModel: OwnerRegistrationViewModel
) {
    val isEditing = ownerId > 0L
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Campos do formulário
    var nome by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var nomeCondominio by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    // Estados de erro de validação
    var nomeError by remember { mutableStateOf(false) }
    var enderecoError by remember { mutableStateOf(false) }
    var telefoneError by remember { mutableStateOf(false) }

    // CEP loading state
    var isCepLoading by remember { mutableStateOf(false) }

    // Seletor de Foto
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it.toString() }
    }

    // Carrega dados existentes para edição
    LaunchedEffect(ownerId) {
        if (ownerId > 0L) {
            val owner = viewModel.loadOwner(ownerId)
            if (owner != null) {
                nome = owner.nome
                endereco = owner.endereco
                cep = owner.cep
                telefone = owner.telefone
                nomeCondominio = owner.nomeCondominio
                apartamento = owner.apartamento
                photoUri = owner.photoUri
            }
        }
    }

    // Consulta CEP automática
    LaunchedEffect(cep) {
        val cleanCep = cep.replace(Regex("[^0-9]"), "")
        if (cleanCep.length == 8 && endereco.isBlank()) {
            isCepLoading = true
            delay(500) // debounce
            try {
                val result = ViaCepClient.consultar(cleanCep)
                if (!result.erro && result.enderecoCompleto.isNotBlank()) {
                    endereco = result.enderecoCompleto
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isCepLoading = false
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is OwnerRegistrationUIState.Success) {
            onCadastroConcluido((uiState as OwnerRegistrationUIState.Success).owner.id)
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
                title = {
                    Text(
                        if (isEditing) "Editar Proprietário" else "Cadastro do Proprietário",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(GradientAmber))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isEditing) Icons.Default.Edit else Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                if (isEditing) "Editar Proprietário" else "Cadastrar Proprietário",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Gere o QR Code para contato via WhatsApp",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Photo Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto do proprietário",
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Adicionar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Foto do Proprietário", style = MaterialTheme.typography.bodySmall, color = Slate400)
                }
            }

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Dados do Proprietário",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            nomeError = false
                        },
                        label = { Text("Nome Completo *") },
                        placeholder = { Text("Ex: João Silva") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nomeError,
                        supportingText = { if (nomeError) Text("Campo obrigatório") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    // CEP com busca
                    OutlinedTextField(
                        value = cep,
                        onValueChange = { input ->
                            val clean = input.replace(Regex("[^0-9]"), "")
                            if (clean.length <= 8) {
                                cep = when {
                                    clean.length > 5 -> "${clean.take(5)}-${clean.substring(5)}"
                                    else -> clean
                                }
                            }
                        },
                        label = { Text("CEP") },
                        placeholder = { Text("00000-000") },
                        leadingIcon = {
                            if (isCepLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        trailingIcon = {
                            if (cep.replace(Regex("[^0-9]"), "").length == 8 && !isCepLoading) {
                                IconButton(onClick = {
                                    isCepLoading = true
                                    val cleanCep = cep.replace(Regex("[^0-9]"), "")
                                    coroutineScope.launch {
                                        try {
                                            val result = ViaCepClient.consultar(cleanCep)
                                            if (!result.erro && result.enderecoCompleto.isNotBlank()) {
                                                endereco = result.enderecoCompleto
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        isCepLoading = false
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Consultar CEP",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        supportingText = {
                            if (isCepLoading) Text("Consultando CEP...", color = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = endereco,
                        onValueChange = {
                            endereco = it
                            enderecoError = false
                        },
                        label = { Text("Endereço Completo *") },
                        placeholder = { Text("Rua, número, bairro, cidade - UF") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = enderecoError,
                        supportingText = { if (enderecoError) Text("Campo obrigatório") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = nomeCondominio,
                        onValueChange = { nomeCondominio = it },
                        label = { Text("Condomínio / Residencial") },
                        placeholder = { Text("Opcional") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = apartamento,
                        onValueChange = { apartamento = it },
                        label = { Text("Apartamento / Unidade") },
                        placeholder = { Text("Ex: 101, Bloco A") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = telefone,
                        onValueChange = { input ->
                            val clean = input.replace(Regex("[^0-9]"), "")
                            if (clean.length <= 11) {
                                telefone = when {
                                    clean.length > 7 -> "(${clean.take(2)}) ${clean.substring(2, clean.length - 4)}-${clean.substring(clean.length - 4)}"
                                    clean.length > 2 -> "(${clean.take(2)}) ${clean.substring(2)}"
                                    else -> clean
                                }
                                telefoneError = false
                            }
                        },
                        label = { Text("Celular *") },
                        placeholder = { Text("(11) 99999-9999") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = telefoneError,
                        supportingText = { if (telefoneError) Text("Telefone inválido (mín. 10 dígitos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = textFieldColors
                    )
                }
            }

            if (uiState is OwnerRegistrationUIState.Error) {
                Text(
                    text = (uiState as OwnerRegistrationUIState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (nome.isBlank()) nomeError = true
                    if (endereco.isBlank()) enderecoError = true
                    if (telefone.isBlank() || telefone.replace(Regex("[^0-9]"), "").length < 10) telefoneError = true

                    if (!nomeError && !enderecoError && !telefoneError) {
                        viewModel.registerOwner(
                            id = ownerId,
                            nome = nome,
                            nomeCondominio = nomeCondominio,
                            endereco = endereco,
                            cep = cep,
                            apartamento = apartamento,
                            telefone = telefone,
                            photoUri = photoUri
                        )
                    }
                },
                enabled = uiState !is OwnerRegistrationUIState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (uiState is OwnerRegistrationUIState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (isEditing) "ATUALIZAR QR CODE" else "GERAR QR CODE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AppSignature()
        }
    }
}
