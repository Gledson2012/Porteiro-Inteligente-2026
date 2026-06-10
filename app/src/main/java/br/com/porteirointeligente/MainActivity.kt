package br.com.porteirointeligente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.porteirointeligente.ui.home.HomeScreen
import br.com.porteirointeligente.ui.owner.ProfileScreen
import br.com.porteirointeligente.ui.scanner.ScannerScreen
import br.com.porteirointeligente.ui.settings.SettingsScreen
import br.com.porteirointeligente.ui.theme.PorteiroInteligenteTheme
import br.com.porteirointeligente.ui.visit.VisitHistoryScreen
import br.com.porteirointeligente.ui.visit.VisitRegistrationScreen
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import br.com.porteirointeligente.util.AppTheme
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by appViewModel.themeState.collectAsState()
            val darkTheme = when (themeState) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            PorteiroInteligenteTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Início", Icons.Default.Home)
    object History : Screen("history", "Histórico", Icons.Default.History)
    object Profile : Screen("profile", "Perfil/QR", Icons.Default.QrCode)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
    object Scanner : Screen("scanner", "Scanner", Icons.Default.QrCodeScanner)
    object VisitRegistration : Screen("visit_registration", "Registrar Visita", Icons.Default.Add)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Profile,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 0.dp) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                    onNavigateToVisitRegistration = { navController.navigate(Screen.VisitRegistration.route) }
                )
            }
            composable(Screen.History.route) { VisitHistoryScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Scanner.route) { 
                ScannerScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.VisitRegistration.route) {
                VisitRegistrationScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
