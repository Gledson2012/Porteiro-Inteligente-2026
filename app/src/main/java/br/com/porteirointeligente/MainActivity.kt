package br.com.porteirointeligente

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import br.com.porteirointeligente.ui.navigation.RootNavGraph
import br.com.porteirointeligente.ui.theme.PorteiroInteligenteTheme
import br.com.porteirointeligente.util.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by appViewModel.themeState.collectAsState()
            val useDynamicColor by appViewModel.dynamicColorState.collectAsState()
            val flagSecure by appViewModel.flagSecureState.collectAsState()

            LaunchedEffect(flagSecure) {
                if (flagSecure) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

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
