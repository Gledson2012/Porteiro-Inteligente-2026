package br.com.porteirointeligente

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class do Porteiro Inteligente.
 *
 * A anotação [HiltAndroidApp] inicializa o sistema de injeção de dependência
 * do Hilt, gerando os componentes necessários para a aplicação inteira.
 */
@HiltAndroidApp
class PorteiroInteligenteApp : Application()
