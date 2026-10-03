package br.com.porteirointeligente.ui.owner

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.ui.components.AppSignature
import br.com.porteirointeligente.ui.components.ShimmerProfileCard
import br.com.porteirointeligente.ui.theme.GradientNeon
import br.com.porteirointeligente.ui.theme.TextMuted
import br.com.porteirointeligente.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream

/**
 * Tela de Exibição do QR Code.
 *
 * Conforme especificação do prompt:
 * - QR Code centralizado em card com cantos arredondados
 * - URL protegida (LGPD): dados pessoais ficam dentro do payload cifrado
 * - Botão "SALVAR OU COMPARTILHAR" que aciona Intent nativo
 *
 * ═══════════════════════════════════════════════
 * Arquivo criado conforme especificação do prompt:
 * "Fluxo 2: Tela de Exibição do QR Code (QrCodeScreen.kt)"
 * ═══════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu QR Code", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is OwnerDetailsViewModel.OwnerDetailsUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    ShimmerProfileCard()
                }
            }
            is OwnerDetailsViewModel.OwnerDetailsUiState.Empty -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextMuted)
                        Spacer(Modifier.height(16.dp))
                        Text("Nenhum morador cadastrado", style = MaterialTheme.typography.titleMedium, color = TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Text("Cadastre um morador para gerar o QR Code", style = MaterialTheme.typography.bodySmall, color = TextMuted.copy(alpha = 0.7f))
                    }
                }
            }
            is OwnerDetailsViewModel.OwnerDetailsUiState.Success -> {
                val context = LocalContext.current
                var showZoomDialog by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Resident Selector Dropdown if there are multiple owners
                    if (state.allOwners.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                "Visualizando QR Code de:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                state.owner.nome,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Selecionar outro morador"
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                state.allOwners.forEach { owner ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(owner.nome, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Ap. ${owner.apartamento} • ${owner.nomeCondominio}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selecionarOwner(owner.id)
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            if (owner.id == state.owner.id) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Icon(Icons.Default.Person, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Mostre este código para permitir que visitantes entrem em contato com você.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // === Card do QR Code ===
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            // Header com gradiente Neon
                            Box(
                                modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(GradientNeon)).padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(text = state.owner.nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "Ap. ${state.owner.apartamento}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                                }
                            }

                            // QR Code gerado com ZXing (Toque para ampliar)
                            if (state.qrCode != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 20.dp, bottom = 6.dp)
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        .clickable { showZoomDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = state.qrCode.asImageBitmap(),
                                        contentDescription = "QR Code do morador - Toque para ampliar",
                                        modifier = Modifier.size(220.dp).padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showZoomDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Toque no código para ampliar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // URL mascarada (LGPD - sem dados pessoais)
                            Surface(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "porteiro-inteligente-2026.vercel.app/scan",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Surface(
                                color = if (state.owner.isCurrentlyOffline()) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.owner.isCurrentlyOffline()) Icons.Default.CloudOff else Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (state.owner.isCurrentlyOffline()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (state.owner.isCurrentlyOffline()) "Modo offline ativo" else "Código ativo e protegido",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // === Ações do QR Code ===
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                state.qrCode?.let { bitmap ->
                                    try {
                                        val cacheDir = File(context.cacheDir, "shared_images")
                                        cacheDir.mkdirs()
                                        val file = File(cacheDir, "qrcode_${state.owner.id}.png")
                                        FileOutputStream(file).use { out ->
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                        }
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "🔔 PORTEIRO INTELIGENTE\nMorador: ${state.owner.nome}\nAp. ${state.owner.apartamento}\n\nEscaneie este QR Code para me chamar diretamente no WhatsApp!\nLink: ${state.owner.qrCodePayload}"
                                            )
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar QR Code"))
                                    } catch (e: Exception) {
                                        Log.e("QRCODE_SHARE", "Erro ao compartilhar QR Code", e)
                                        Toast.makeText(context, "Não foi possível compartilhar o QR Code", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Compartilhar", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = {
                                state.qrCode?.let { bitmap ->
                                    saveQrCodeToGallery(context, bitmap, state.owner)
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Salvar PNG", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(ClipboardManager::class.java)
                                clipboard?.setPrimaryClip(
                                    ClipData.newPlainText("Link do QR Code", state.owner.qrCodePayload)
                                )
                                Toast.makeText(context, "Link copiado para a área de transferência", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copiar Link", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Modal de visualização ampliada (Full-Screen Zoom)
                    if (showZoomDialog && state.qrCode != null) {
                        Dialog(onDismissRequest = { showZoomDialog = false }) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier.fillMaxWidth().padding(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = state.owner.nome,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Apto ${state.owner.apartamento}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { showZoomDialog = false }) {
                                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(260.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = state.qrCode.asImageBitmap(),
                                            contentDescription = "QR Code Ampliado",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Text(
                                        text = "Aponte a câmera do smartphone para escanear",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Button(
                                        onClick = { showZoomDialog = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Fechar")
                                    }
                                }
                            }
                        }
                    }

                    // === Instruções LGPD ===
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Conformidade LGPD",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Seus dados pessoais (como telefone) estão criptografados de forma segura na URL, garantindo sua privacidade.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Assinatura
                    AppSignature()
                }
            }
        }
    }
}

private fun saveQrCodeToGallery(
    context: android.content.Context,
    bitmap: Bitmap,
    owner: br.com.porteirointeligente.domain.model.Owner
) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "qrcode_${owner.apartamento}_${owner.nome.replace(" ", "_")}.png")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/PorteiroInteligente")
            }
            val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(context, "QR Code salvo em Imagens/PorteiroInteligente!", Toast.LENGTH_LONG).show()
                return
            }
        }
        val cacheDir = File(context.cacheDir, "shared_images")
        cacheDir.mkdirs()
        val file = File(cacheDir, "qrcode_${owner.id}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Toast.makeText(context, "QR Code salvo com sucesso!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("QRCODE_SAVE", "Erro ao salvar QR Code", e)
        Toast.makeText(context, "Não foi possível salvar o QR Code", Toast.LENGTH_SHORT).show()
    }
}
