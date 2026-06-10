package br.com.porteirointeligente.ui.owner

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = owner.photoUri,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentScale = ContentScale.Crop
        )

        Text(
            text = owner.nome,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Apto: ${owner.apartamento}",
            style = MaterialTheme.typography.bodyLarge
        )

        qrCode?.let {
            Card(
                modifier = Modifier.size(250.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
        }

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
                Text("Editar")
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

@Composable
fun OwnerRegistrationForm(
    owner: Owner?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: OwnerRegistrationViewModel
) {
    var nome by remember { mutableStateOf(owner?.nome ?: "") }
    var endereco by remember { mutableStateOf(owner?.endereco ?: "") }
    var cep by remember { mutableStateOf(owner?.cep ?: "") }
    var apartamento by remember { mutableStateOf(owner?.apartamento ?: "") }
    var telefone by remember { mutableStateOf(owner?.telefone ?: "") }
    var photoUri by remember { mutableStateOf(owner?.photoUri) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri?.toString() }

    LaunchedEffect(viewModel.registrationEvent) {
        viewModel.registrationEvent.collect { event ->
            if (event is OwnerRegistrationViewModel.RegistrationUiEvent.Success) {
                onSave()
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
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endereco,
            onValueChange = { endereco = it },
            label = { Text("Endereço") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cep,
                onValueChange = { cep = it },
                label = { Text("CEP") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = apartamento,
                onValueChange = { apartamento = it },
                label = { Text("Apto") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = telefone,
            onValueChange = { telefone = it },
            label = { Text("Telefone") },
            modifier = Modifier.fillMaxWidth()
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
