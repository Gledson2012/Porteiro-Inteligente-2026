package br.com.porteirointeligente.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import br.com.porteirointeligente.ui.visit.VisitHistoryScreen
import br.com.porteirointeligente.ui.visit.VisitRegistrationScreen

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "In\u00edcio", Icons.Filled.Home, Icons.Outlined.Home)
    object History : Screen("history", "Hist\u00f3rico", Icons.Filled.History, Icons.Outlined.History)
    object Profile : Screen("profile", "Perfil/QR", Icons.Filled.QrCode, Icons.Outlined.QrCode)
    object Settings : Screen("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
    object Scanner : Screen("scanner", "Scanner", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    object VisitRegistration : Screen("visit_registration", "Registrar Visita", Icons.Filled.Add, Icons.Outlined.Add)
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
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
                    onNavigateToVisitRegistration = { navController.navigate(Screen.VisitRegistration.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
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
