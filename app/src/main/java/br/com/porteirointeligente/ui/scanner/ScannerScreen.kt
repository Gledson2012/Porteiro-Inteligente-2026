package br.com.porteirointeligente.ui.scanner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.util.QrCodeAnalyzer
import br.com.porteirointeligente.ui.theme.GradientGold
import br.com.porteirointeligente.ui.theme.ScannerOverlay
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }
    var isTorchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var showOfflineDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val scanLineAnim = rememberInfiniteTransition(label = "scan_line")
    val scanLineOffset by scanLineAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line_offset"
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ScannerViewModel.ScannerUiEvent.OpenWhatsApp -> {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.url)))
                        onNavigateBack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro ao abrir link", Toast.LENGTH_SHORT).show()
                        isScanning = true
                    }
                }
                is ScannerViewModel.ScannerUiEvent.ShowOfflineMessage -> {
                    showOfflineDialog = event.message to event.url
                }
                is ScannerViewModel.ScannerUiEvent.InvalidQrCode -> {
                    Toast.makeText(context, "QR Code inválido. Escaneie um QR Code de morador.", Toast.LENGTH_SHORT).show()
                    isScanning = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera view
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrContent ->
                                    if (isScanning) {
                                        isScanning = false
                                        viewModel.onQrCodeDetected(qrContent)
                                    }
                                })
                            }

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ScannerOverlay,
                            ScannerOverlay.copy(alpha = 0.3f),
                            ScannerOverlay.copy(alpha = 0.3f),
                            ScannerOverlay
                        ),
                        startY = 0f,
                        endY = 1200f
                    )
                )
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                if (hasCameraPermission) {
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            camera?.cameraControl?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTorchOn) Color.White.copy(alpha = 0.9f)
                                else Color.Black.copy(alpha = 0.3f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (isTorchOn) "Desligar lanterna" else "Ligar lanterna",
                            tint = if (isTorchOn) Color.Black else Color.White
                        )
                    }
                }
            }

            // Scan frame
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color.Transparent)
                ) {
                    // Corner borders
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.TopStart)
                        )
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.TopStart)
                                .padding(start = 0.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.TopEnd)
                        )
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.TopEnd)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomStart)
                            .background(Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomStart)
                        )
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomStart)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomEnd)
                        )
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    // Scanning line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .graphicsLayer {
                                translationY = size.height.toFloat() * scanLineOffset
                            }
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.primary,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            // Bottom instructions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Aponte a câmera para o QR Code do morador",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "O contato será aberto automaticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Permission denied
        if (!hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Permissão de câmera necessária",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Conceder Permissão")
                    }
                }
            }
        }
    }

    // Offline dialog
    showOfflineDialog?.let { (message, url) ->
        AlertDialog(
            onDismissRequest = {
                showOfflineDialog = null
                isScanning = true
            },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Morador Offline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    message.ifBlank { "O morador está offline no momento e pode não receber sua mensagem." }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOfflineDialog = null
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            onNavigateBack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao abrir link", Toast.LENGTH_SHORT).show()
                            isScanning = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Abrir mesmo assim")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showOfflineDialog = null
                    onNavigateBack()
                }) {
                    Text("Voltar")
                }
            }
        )
    }
}
