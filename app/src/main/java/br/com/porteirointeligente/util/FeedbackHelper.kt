package br.com.porteirointeligente.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Utilitário de feedback sensorial (háptico e sonoro) para o scanner de QR Code
 * e ações de confirmação no aplicativo.
 */
object FeedbackHelper {

    /**
     * Emite vibração tátil e bip sonoro ao reconhecer um QR Code com sucesso.
     */
    fun playScanSuccess(context: Context) {
        // Feedback háptico (vibração)
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {
            // Permissão ou hardware de vibração não disponível
        }

        // Feedback sonoro (bip)
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (_: Exception) {
            // Dispositivo em modo silencioso ou sem suporte
        }
    }
}
