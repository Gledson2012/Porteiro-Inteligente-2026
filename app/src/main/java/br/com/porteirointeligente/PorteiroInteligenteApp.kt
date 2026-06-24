package br.com.porteirointeligente

import android.app.Application
import br.com.porteirointeligente.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class do Porteiro Inteligente.
 *
 * A anotação [HiltAndroidApp] inicializa o sistema de injeção de dependência
 * do Hilt, gerando os componentes necessários para a aplicação inteira.
 */
@HiltAndroidApp
class PorteiroInteligenteApp : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()

        // Inicializa os canais de notificação na primeira execução
        notificationHelper.createNotificationChannels()
    }
}
