package br.com.porteirointeligente.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import br.com.porteirointeligente.ui.owner.OwnerManagementScreen
import br.com.porteirointeligente.ui.owner.ProfileScreen
import br.com.porteirointeligente.ui.scanner.ScannerScreen
import br.com.porteirointeligente.ui.settings.SettingsScreen
import br.com.porteirointeligente.ui.onboarding.OnboardingScreen
import br.com.porteirointeligente.ui.splash.SplashScreen
import br.com.porteirointeligente.ui.visit.VisitHistoryScreen
import br.com.porteirointeligente.ui.visit.VisitRegistrationScreen

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Splash : Screen("splash", "Splash", Icons.Filled.Home, Icons.Outlined.Home)
    object Home : Screen("home", "In\u00edcio", Icons.Filled.Home, Icons.Outlined.Home)
    object History : Screen("history", "Hist\u00f3rico", Icons.Filled.History, Icons.Outlined.History)
    object Profile : Screen("profile", "Perfil/QR", Icons.Filled.QrCode, Icons.Outlined.QrCode)
    object Settings : Screen("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
    object Scanner : Screen("scanner", "Scanner", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    object VisitRegistration : Screen("visit_registration", "Registrar Visita", Icons.Filled.Add, Icons.Outlined.Add)
    object OwnerManagement : Screen("owner_management", "Gerenciar", Icons.Filled.People, Icons.Outlined.People)
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Filled.Star, Icons.Outlined.Star)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val onboardingViewModel: br.com.porteirointeligente.ui.onboarding.OnboardingViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val shouldShowOnboarding by onboardingViewModel.shouldShowOnboarding.collectAsState()

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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Screen.Splash.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                SplashScreen(onSplashFinished = {
                    val destination = if (shouldShowOnboarding) Screen.Onboarding.route else Screen.Home.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(
                route = Screen.Onboarding.route,
                enterTransition = { fadeIn(animationSpec = tween(500)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                OnboardingScreen(
                    onOnboardingFinished = {
                        onboardingViewModel.completeOnboarding()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn(animationSpec = tween(500)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                HomeScreen(
                    onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                    onNavigateToVisitRegistration = { navController.navigate(Screen.VisitRegistration.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.History.route) { VisitHistoryScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToOwnerManagement = {
                        navController.navigate(Screen.OwnerManagement.route)
                    }
                )
            }
            composable(Screen.Scanner.route) {
                ScannerScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.VisitRegistration.route) {
                VisitRegistrationScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.OwnerManagement.route) {
                OwnerManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditOwner = { owner ->
                        // Navega de volta ao perfil - o usuário edita pela ProfileScreen
                        navController.navigate(Screen.Profile.route) {
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
}
