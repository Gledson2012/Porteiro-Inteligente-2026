package br.com.porteirointeligente.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.porteirointeligente.ui.splash.SplashScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashScreen_showsAppName() {
        composeTestRule.setContent {
            SplashScreen(onSplashFinished = {})
        }

        // Verifica se o nome do app é exibido
        composeTestRule.onNodeWithText("Porteiro Inteligente").assertExists()
    }

    @Test
    fun splashScreen_showsSubtitle() {
        composeTestRule.setContent {
            SplashScreen(onSplashFinished = {})
        }

        // Verifica se o subtítulo é exibido
        composeTestRule.onNodeWithText("Gestão de Portaria Simplificada").assertExists()
    }

    @Test
    fun splashScreen_showsTagline() {
        composeTestRule.setContent {
            SplashScreen(onSplashFinished = {})
        }

        // Verifica se as tags de funcionalidades são exibidas
        composeTestRule.onNodeWithText("QR Code • Visitas • Offline").assertExists()
    }
}
