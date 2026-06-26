package br.com.porteirointeligente.ui.owner

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
 * - URL mascarada (LGPD): apenas ID, sem dados pessoais
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
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") } },
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

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                            // QR Code gerado com ZXing
                            if (state.qrCode != null) {
                                Box(
                                    modifier = Modifier.padding(24.dp).size(240.dp).clip(RoundedCornerShape(16.dp)).background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = state.qrCode.asImageBitmap(),
                                        contentDescription = "QR Code do morador",
                                        modifier = Modifier.size(220.dp).padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            // URL mascarada (LGPD - sem dados pessoais)
                            Text(
                                text = state.owner.qrCodePayload,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )

                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // === Botão SALVAR OU COMPARTILHAR ===
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
                                        putExtra(Intent.EXTRA_TEXT, "QR Code do ${state.owner.nome} - Ap. ${state.owner.apartamento}\n\nEscaneeie para entrar em contato via WhatsApp")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartilhar QR Code"))
                                } catch (e: Exception) {
                                    Log.e("QRCODE_SHARE", "Erro ao compartilhar QR Code", e)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "SALVAR OU COMPARTILHAR",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // === Instruções LGPD ===
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Protegido pela LGPD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text("Seus dados pessoais não estão no QR Code", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
