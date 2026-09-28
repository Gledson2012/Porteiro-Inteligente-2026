package br.com.porteirointeligente.ui.visit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.ui.theme.GradientTeal
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitRegistrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: VisitRegistrationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var nome by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var apartamento by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    
    var nomeError by remember { mutableStateOf<String?>(null) }
    var apartamentoError by remember { mutableStateOf<String?>(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successOwnerPhone by remember { mutableStateOf<String?>(null) }
    var successOwnerName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is VisitRegistrationUIState.Success -> {
                if (!state.ownerPhone.isNullOrBlank()) {
                    successOwnerPhone = state.ownerPhone
                    successOwnerName = state.ownerName
                    showSuccessDialog = true
                } else {
                    onNavigateBack()
                }
            }
            is VisitRegistrationUIState.Error -> {
                if (nome.isBlank()) nomeError = "Campo obrigatório"
                if (apartamento.isBlank()) apartamentoError = "Campo obrigatório"
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateBack()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Entrada Registrada!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A visita de $nome para o Apto $apartamento foi registrada com sucesso.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    successOwnerName?.let {
                        Text(
                            text = "Deseja avisar o morador ($it) no WhatsApp agora?",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        val cleanPhone = (successOwnerPhone ?: "").filter { it.isDigit() }
                        val hora = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date())
                        val msg = "Olá! Informamos que seu visitante $nome acabou de se apresentar na portaria (Apto $apartamento) às $hora."
                        val encodedMsg = URLEncoder.encode(msg, "UTF-8")
                        val whatsappUrl = "https://wa.me/55$cleanPhone?text=$encodedMsg"

                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        onNavigateBack()
                    }
                ) {
                    Text("Avisar no WhatsApp")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Concluir")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Visita", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = GradientTeal,
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Nova Visita",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Identifique o visitante e registre a entrada na portaria",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Form container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                // Nome
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        nomeError = null
                    },
                    label = { Text("Nome do Visitante *") },
                    placeholder = { Text("Ex: Carlos Silva") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nomeError != null,
                    supportingText = nomeError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = textFieldColors
                )

                // Documento (RG/CPF)
                OutlinedTextField(
                    value = documento,
                    onValueChange = { documento = it },
                    label = { Text("Documento (RG / CPF)") },
                    placeholder = { Text("Ex: 12.345.678-9") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = textFieldColors
                )

                // Apartamento
                OutlinedTextField(
                    value = apartamento,
                    onValueChange = {
                        apartamento = it
                        apartamentoError = null
                    },
                    label = { Text("Apartamento *") },
                    placeholder = { Text("Ex: 102 Bloco B") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    isError = apartamentoError != null,
                    supportingText = apartamentoError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = textFieldColors
                )

                // Placa do Veículo (Mercosul ou antiga)
                OutlinedTextField(
                    value = placa,
                    onValueChange = { input ->
                        val clean = input.filter { it.isLetterOrDigit() }.uppercase().take(7)
                        placa = if (clean.length > 3) {
                            "${clean.substring(0, 3)}-${clean.substring(3)}"
                        } else {
                            clean
                        }
                    },
                    label = { Text("Placa do Veículo (Opcional)") },
                    placeholder = { Text("Ex: ABC-1D23 ou ABC-1234") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = textFieldColors
                )

                // Telefone
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
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
                    label = { Text("Telefone / WhatsApp (Opcional)") },
                    placeholder = { Text("(11) 99999-8888") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = textFieldColors
                )

                // Motivo
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo da Visita") },
                    placeholder = { Text("Ex: Entrega, Visita familiar, Prestador de serviço...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = textFieldColors
                )

                Spacer(Modifier.height(8.dp))

                // Register button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.registrarVisita(nome, documento, apartamento, telefone, motivo, placa)
                    },
                    enabled = uiState !is VisitRegistrationUIState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (uiState is VisitRegistrationUIState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "REGISTRAR ENTRADA",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
