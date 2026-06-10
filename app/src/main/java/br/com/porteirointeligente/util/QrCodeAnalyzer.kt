package br.com.porteirointeligente.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * Analisador de frames da câmera para detectar QR Codes usando ZXing.
 */
class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    override fun analyze(image: ImageProxy) {
        val buffer: ByteBuffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val source = PlanarYUVLuminanceSource(
            data,
            image.width,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(binaryBitmap)
            onQrCodeDetected(result.text)
        } catch (e: Exception) {
            // Nenhum QR Code encontrado neste frame
        } finally {
            image.close()
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
