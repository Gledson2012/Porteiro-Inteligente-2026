package br.com.porteirointeligente.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.com.porteirointeligente.MainActivity
import br.com.porteirointeligente.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de notificações do Porteiro Inteligente.
 *
 * Cria canais de notificação e exibe notificações locais e remotas (FCM).
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_VISITS_ID = "novas_visitas"
        private const val CHANNEL_VISITS_NAME = "Novas Visitas"
        private const val CHANNEL_VISITS_DESC = "Notificações quando uma nova visita é registrada"
        private const val NOTIFICATION_ID_VISIT = 1001
    }

    /**
     * Cria os canais de notificação (necessário no Android 8+).
     * Deve ser chamado no Application.onCreate()
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_VISITS_ID,
                CHANNEL_VISITS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_VISITS_DESC
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Exibe uma notificação local sobre uma nova visita.
     */
    fun showVisitNotification(visitanteNome: String, apartamento: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_VISITS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nova visita registrada")
            .setContentText("$visitanteNome • Apto $apartamento")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$visitanteNome acabou de registrar entrada para o apartamento $apartamento.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_VISIT, notification)
        } catch (e: SecurityException) {
            // Permissão POST_NOTIFICATIONS não concedida (Android 13+)
        }
    }
}
