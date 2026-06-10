package br.com.porteirointeligente.ui.scanner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.porteirointeligente.util.QrCodeAnalyzer
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
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
    var showOfflineDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
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
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner de QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                                cameraProvider.bindToLifecycle(
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
                
                // Overlay para ajudar a mirar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Aqui poderia ter uma borda desenhada ou algo similar
                }
            } else {
                Text(
                    "Permissão de câmera necessária",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    showOfflineDialog?.let { (message, url) ->
        AlertDialog(
            onDismissRequest = { 
                showOfflineDialog = null
                isScanning = true
            },
            title = { Text("Morador Offline") },
            text = { Text(message.ifBlank { "O morador está offline no momento." }) },
            confirmButton = {
                TextButton(onClick = {
                    showOfflineDialog = null
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        onNavigateBack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro ao abrir link", Toast.LENGTH_SHORT).show()
                        isScanning = true
                    }
                }) {
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
