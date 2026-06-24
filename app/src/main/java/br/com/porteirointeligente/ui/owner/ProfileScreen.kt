package br.com.porteirointeligente.ui.owner

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.print.PrintHelper
import coil.compose.AsyncImage
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.StringUtils
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    detailsViewModel: OwnerDetailsViewModel = hiltViewModel(),
    registrationViewModel: OwnerRegistrationViewModel = hiltViewModel()
) {
    val uiState by detailsViewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editingOwner by remember { mutableStateOf<Owner?>(null) }
    var showOwnerList by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // O init do ViewModel já carrega os moradores
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingOwner = null
                    isEditing = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar morador")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is OwnerDetailsViewModel.OwnerDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is OwnerDetailsViewModel.OwnerDetailsUiState.Success -> {
                    if (isEditing) {
                        OwnerRegistrationForm(
                            owner = editingOwner,
                            onSave = {
                                isEditing = false
                                editingOwner = null
                            },
                            onCancel = {
                                isEditing = false
                                editingOwner = null
                            },
                            viewModel = registrationViewModel
                        )
                    } else {
                        OwnerDetailsView(
                            owner = state.owner,
                            qrCode = state.qrCode,
                            allOwners = state.allOwners,
                            onEdit = {
                                editingOwner = state.owner
                                isEditing = true
                            },
                            onDelete = { detailsViewModel.deleteOwner(state.owner.id) },
                            onSelectOwner = { detailsViewModel.selecionarOwner(it.id) },
                            onShowList = { showOwnerList = !showOwnerList }
                        )

                        if (showOwnerList) {
                            OwnerListOverlay(
                                owners = state.allOwners,
                                selectedOwnerId = state.owner.id,
                                onSelect = {
                                    detailsViewModel.selecionarOwner(it.id)
                                    showOwnerList = false
                                },
                                onDismiss = { showOwnerList = false }
                            )
                        }
                    }
                }
                is OwnerDetailsViewModel.OwnerDetailsUiState.Empty -> {
                    if (isEditing) {
                        OwnerRegistrationForm(
                            owner = null,
                            onSave = {
                                isEditing = false
                            },
                            onCancel = { isEditing = false },
                            viewModel = registrationViewModel
                        )
                    } else {
                        // Tela vazia com botão de cadastro
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Nenhum morador cadastrado",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Cadastrar Morador")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerListOverlay(
    owners: List<Owner>,
    selectedOwnerId: Long,
    onSelect: (Owner) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Moradores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                owners.forEach { owner ->
                    OwnerListItem(
                        owner = owner,
                        isSelected = owner.id == selectedOwnerId,
                        onClick = { onSelect(owner) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OwnerListItem(owner: Owner, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = owner.photoUri,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = owner.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Apto ${owner.apartamento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OwnerDetailsView(
    owner: Owner,
    qrCode: Bitmap?,
    allOwners: List<Owner> = emptyList(),
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelectOwner: (Owner) -> Unit = {},
    onShowList: () -> Unit = {}
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
        ) {
            if (allOwners.size > 1) {
                IconButton(
                    onClick = onShowList,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Trocar morador",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

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

            // Indicador de múltiplos moradores
            if (allOwners.size > 1) {
                AssistChip(
                    onClick = onShowList,
                    label = { Text("${allOwners.size} moradores") },
                    leadingIcon = {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }

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
                    DetailRow(icon = Icons.Default.Phone, label = "Contato", value = StringUtils.formatPhone(owner.telefone))
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val qrBitmap = it
                            val context = LocalContext.current

                            OutlinedButton(
                                onClick = { saveQrToGallery(context, qrBitmap) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Salvar", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { shareQrCode(context, qrBitmap) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Compartilhar", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { printQrCode(context, qrBitmap) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Imprimir", style = MaterialTheme.typography.labelSmall)
                            }
                        }
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

private fun saveQrToGallery(context: Context, bitmap: Bitmap) {
    try {
        val filename = "QR_Porteiro_${System.currentTimeMillis()}.jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PorteiroInteligente")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                val updateValues = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                context.contentResolver.update(it, updateValues, null, null)
            }
        } else {
            MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap, filename, "QR Code Porteiro Inteligente"
            )
        }
        Toast.makeText(context, "QR Code salvo na galeria!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao salvar imagem.", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

private fun shareQrCode(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.cacheDir, "qr_code_compartilhar.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar QR Code"))
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao compartilhar.", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

private fun printQrCode(context: Context, bitmap: Bitmap) {
    try {
        val printHelper = PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
        }
        printHelper.printBitmap("QR Code - Porteiro Inteligente", bitmap)
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao imprimir.", Toast.LENGTH_LONG).show()
        e.printStackTrace()
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
    var cep by remember { mutableStateOf(owner?.cep?.let { StringUtils.formatCep(it) } ?: "") }
    var apartamento by remember { mutableStateOf(owner?.apartamento ?: "") }
    var telefone by remember { mutableStateOf(owner?.telefone?.let { StringUtils.formatPhone(it) } ?: "") }
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
            onValueChange = { telefone = StringUtils.maskPhone(it) },
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
