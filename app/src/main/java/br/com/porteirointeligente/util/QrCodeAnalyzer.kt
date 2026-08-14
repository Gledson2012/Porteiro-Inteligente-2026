package br.com.porteirointeligente.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
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
        val plane = image.planes[0]
        val width = image.width
        val height = image.height
        val data = plane.buffer.toLumaByteArray(
            width = width,
            height = height,
            rowStride = plane.rowStride,
            pixelStride = plane.pixelStride
        )
        var source: LuminanceSource = PlanarYUVLuminanceSource(
            data,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )

        // A rotação não configurada no ImageAnalysis pode deixar o QR de lado
        // em alguns aparelhos. ZXing consegue corrigir as rotações de 90°.
        if (source.isRotateSupported) {
            repeat ((image.imageInfo.rotationDegrees / 90) % 4) {
                source = source.rotateCounterClockwise()
            }
        }
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

    private fun ByteBuffer.toLumaByteArray(
        width: Int,
        height: Int,
        rowStride: Int,
        pixelStride: Int
    ): ByteArray {
        val source = duplicate()
        val data = ByteArray(width * height)
        val basePosition = source.position()

        // O buffer YUV frequentemente contém bytes de padding no fim de cada
        // linha. Removê-los evita que o QR fique distorcido no ZXing.
        for (row in 0 until height) {
            val rowStart = basePosition + row * rowStride
            for (column in 0 until width) {
                data[row * width + column] = source.get(rowStart + column * pixelStride)
            }
        }
        return data
    }
}
