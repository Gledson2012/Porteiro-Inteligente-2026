package br.com.porteirointeligente.ui.owner

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import br.com.porteirointeligente.domain.model.Owner

@Composable
fun ProfileScreen(
    detailsViewModel: OwnerDetailsViewModel = hiltViewModel(),
    registrationViewModel: OwnerRegistrationViewModel = hiltViewModel()
) {
    val uiState by detailsViewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        detailsViewModel.loadOwner()
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is OwnerDetailsViewModel.OwnerDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is OwnerDetailsViewModel.OwnerDetailsUiState.Success -> {
                    if (isEditing) {
                        OwnerRegistrationForm(
                            owner = state.owner,
                            onSave = { 
                                isEditing = false
                                detailsViewModel.loadOwner()
                            },
                            onCancel = { isEditing = false },
                            viewModel = registrationViewModel
                        )
                    } else {
                        OwnerDetailsView(
                            owner = state.owner,
                            qrCode = state.qrCode,
                            onEdit = { isEditing = true },
                            onDelete = { detailsViewModel.deleteOwner() }
                        )
                    }
                }
                is OwnerDetailsViewModel.OwnerDetailsUiState.Empty -> {
                    OwnerRegistrationForm(
                        owner = null,
                        onSave = { detailsViewModel.loadOwner() },
                        onCancel = { /* Pode voltar para Home */ },
                        viewModel = registrationViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerDetailsView(
    owner: Owner,
    qrCode: Bitmap?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val gradient = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner Superior com Gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(gradient)
        )

        // Foto de Perfil sobreposta ao Banner
        Box(
            modifier = Modifier
                .offset(y = (-50).dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(3.dp)
        ) {
            AsyncImage(
                model = owner.photoUri,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .offset(y = (-40).dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nome
            Text(
                text = owner.nome,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Detalhes do Morador em um Cartão
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(icon = Icons.Default.Apartment, label = "Condomínio", value = owner.nomeCondominio.ifBlank { "Não informado" })
                    DetailRow(icon = Icons.Default.Home, label = "Unidade", value = "Apto ${owner.apartamento}")
                    DetailRow(icon = Icons.Default.Phone, label = "Contato", value = formatPhone(owner.telefone))
                    if (owner.endereco.isNotBlank()) {
                        DetailRow(icon = Icons.Default.LocationOn, label = "Endereço", value = owner.endereco)
                    }
                }
            }

            // QR Code em Destaque
            qrCode?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Código de Acesso Rápido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                                .padding(12.dp)
                        ) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "Apresente este código para identificação rápida na portaria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Ações do Morador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar Perfil")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Excluir")
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun formatPhone(raw: String): String {
    val clean = raw.replace(Regex("[^0-9]"), "")
    return when {
        clean.length >= 11 -> {
            "(${clean.substring(0, 2)}) ${clean.substring(2, 7)}-${clean.substring(7, 11)}"
        }
        clean.length == 10 -> {
            "(${clean.substring(0, 2)}) ${clean.substring(2, 6)}-${clean.substring(6, 10)}"
        }
        else -> clean
    }
}

private fun formatCep(raw: String): String {
    val clean = raw.replace(Regex("[^0-9]"), "")
    return if (clean.length == 8) {
        "${clean.substring(0, 5)}-${clean.substring(5)}"
    } else {
        clean
    }
}

@Composable
fun OwnerRegistrationForm(
    owner: Owner?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: OwnerRegistrationViewModel
) {
    val context = LocalContext.current
    var nome by remember { mutableStateOf(owner?.nome ?: "") }
    var nomeCondominio by remember { mutableStateOf(owner?.nomeCondominio ?: "") }
    var endereco by remember { mutableStateOf(owner?.endereco ?: "") }
    var cep by remember { mutableStateOf(owner?.cep?.let { formatCep(it) } ?: "") }
    var apartamento by remember { mutableStateOf(owner?.apartamento ?: "") }
    var telefone by remember { mutableStateOf(owner?.telefone?.let { formatPhone(it) } ?: "") }
    var photoUri by remember { mutableStateOf(owner?.photoUri) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri?.toString() }

    LaunchedEffect(viewModel.registrationEvent) {
        viewModel.registrationEvent.collect { event ->
            when (event) {
                is OwnerRegistrationViewModel.RegistrationUiEvent.Success -> {
                    android.widget.Toast.makeText(context, "Cadastro salvo com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                    onSave()
                }
                is OwnerRegistrationViewModel.RegistrationUiEvent.ErrorFields -> {
                    android.widget.Toast.makeText(context, "Preencha todos os campos obrigatórios (*).", android.widget.Toast.LENGTH_LONG).show()
                }
                is OwnerRegistrationViewModel.RegistrationUiEvent.ErrorPhone -> {
                    android.widget.Toast.makeText(context, "Insira um número de telefone com DDD válido.", android.widget.Toast.LENGTH_LONG).show()
                }
                is OwnerRegistrationViewModel.RegistrationUiEvent.ErrorCep -> {
                    android.widget.Toast.makeText(context, "CEP inválido. Insira 8 dígitos.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (owner == null) "Cadastro de Morador" else "Editar Cadastro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Mudar foto")
            }
        }

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome Completo *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nomeCondominio,
            onValueChange = { nomeCondominio = it },
            label = { Text("Nome do Condomínio") },
            leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endereco,
            onValueChange = { endereco = it },
            label = { Text("Endereço *") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cep,
                onValueChange = { input ->
                    val clean = input.replace(Regex("[^0-9]"), "")
                    if (clean.length <= 8) {
                        cep = if (clean.length > 5) {
                            "${clean.take(5)}-${clean.substring(5)}"
                        } else {
                            clean
                        }
                    }
                },
                label = { Text("CEP") },
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            OutlinedTextField(
                value = apartamento,
                onValueChange = { apartamento = it },
                label = { Text("Apto *") },
                modifier = Modifier.weight(1f)
            )
        }

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
            label = { Text("WhatsApp *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.registerOwner(
                        id = owner?.id ?: 0L,
                        nome = nome,
                        nomeCondominio = nomeCondominio,
                        endereco = endereco,
                        cep = cep,
                        apartamento = apartamento,
                        telefone = telefone,
                        photoUri = photoUri
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Salvar")
            }
            if (owner != null) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
