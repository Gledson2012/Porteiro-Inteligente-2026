package br.com.porteirointeligente.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import br.com.porteirointeligente.R
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.databinding.ActivityScannerBinding
import br.com.porteirointeligente.util.QrCodeAnalyzer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Tela de leitura de QR Code usando CameraX com suporte a modo offline.
 */
@AndroidEntryPoint
class ScannerActivity : AppCompatActivity() {

    @Inject lateinit var ownerRepository: OwnerRepository

    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, R.string.scanner_permission_denied, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        checkPermissionAndStart()
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrContent ->
                        if (isScanning) {
                            isScanning = false
                            handleQrCode(qrContent)
                        }
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleQrCode(content: String) {
        lifecycleScope.launch {
            val owner = ownerRepository.observeAllOwners().first().firstOrNull()

            runOnUiThread {
                if (owner != null && owner.isCurrentlyOffline()) {
                    showOfflineMessage(owner.offlineMessage, content)
                } else {
                    openWhatsApp(content)
                }
            }
        }
    }

    private fun showOfflineMessage(message: String, originalContent: String) {
        val finalMessage = if (message.isBlank()) "O morador está offline no momento." else message
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Morador Offline")
            .setMessage(finalMessage)
            .setPositiveButton("Abrir mesmo assim") { _, _ -> openWhatsApp(originalContent) }
            .setNegativeButton("Voltar") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun openWhatsApp(content: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir link: $content", Toast.LENGTH_SHORT).show()
            isScanning = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
