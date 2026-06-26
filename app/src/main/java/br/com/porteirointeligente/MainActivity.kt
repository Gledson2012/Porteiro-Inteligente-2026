package br.com.porteirointeligente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import br.com.porteirointeligente.ui.navigation.RootNavGraph
import br.com.porteirointeligente.ui.theme.PorteiroInteligenteTheme
import br.com.porteirointeligente.util.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by appViewModel.themeState.collectAsState()
            val useDynamicColor by appViewModel.dynamicColorState.collectAsState()
            val darkTheme = when (themeState) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            PorteiroInteligenteTheme(
                darkTheme = darkTheme,
                dynamicColor = useDynamicColor
            ) {
                RootNavGraph()
            }
        }
    }
}
