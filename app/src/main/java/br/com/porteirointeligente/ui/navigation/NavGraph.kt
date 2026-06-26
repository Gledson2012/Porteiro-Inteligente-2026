package br.com.porteirointeligente.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.com.porteirointeligente.ui.home.HomeScreen
import br.com.porteirointeligente.ui.owner.CadastroScreen
import br.com.porteirointeligente.ui.owner.OwnerManagementScreen
import br.com.porteirointeligente.ui.owner.QrCodeScreen
import br.com.porteirointeligente.ui.scanner.ScannerScreen
import br.com.porteirointeligente.ui.settings.SettingsScreen
import br.com.porteirointeligente.ui.visit.VisitHistoryScreen
import br.com.porteirointeligente.ui.visit.VisitRegistrationScreen

@Composable
fun MainScreenNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val selected = when (item) {
                        bottomNavItems[0] -> currentDestination?.hasRoute<Home>() == true
                        bottomNavItems[1] -> currentDestination?.hasRoute<History>() == true
                        bottomNavItems[2] -> currentDestination?.hasRoute<QrCodeDisplay>() == true
                        bottomNavItems[3] -> currentDestination?.hasRoute<Settings>() == true
                        else -> false
                    }

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selected,
                        onClick = {
                            val startId = navController.graph.findStartDestination().id
                            when (item) {
                                bottomNavItems[0] -> navController.navigate(Home) {
                                    popUpTo(startId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                bottomNavItems[1] -> navController.navigate(History) {
                                    popUpTo(startId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                bottomNavItems[2] -> navController.navigate(QrCodeDisplay) {
                                    popUpTo(startId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                bottomNavItems[3] -> navController.navigate(Settings) {
                                    popUpTo(startId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
            startDestination = Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Home>(
                enterTransition = {
                    fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.95f, animationSpec = tween(500))
                },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                HomeScreen(
                    onNavigateToScanner = { navController.navigate(Scanner) },
                    onNavigateToRegisterVisit = { navController.navigate(VisitRegistration) },
                    onNavigateToHistory = { navController.navigate(History) },
                    onNavigateToOwners = { navController.navigate(OwnerManagement) }
                )
            }

            composable<History>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) {
                VisitHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRegister = { navController.navigate(VisitRegistration) }
                )
            }

            composable<QrCodeDisplay>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) {
                QrCodeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Settings>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Scanner>(
                enterTransition = {
                    scaleIn(initialScale = 0.85f, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    scaleOut(targetScale = 0.85f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) {
                ScannerScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable<VisitRegistration>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) {
                VisitRegistrationScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable<Cadastro>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val cadastro: Cadastro = backStackEntry.toRoute()
                CadastroScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCadastroConcluido = { id ->
                        navController.navigate(QrCodeDisplay) {
                            popUpTo<Home> { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    ownerId = cadastro.ownerId,
                    viewModel = hiltViewModel()
                )
            }

            composable<OwnerManagement>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) {
                OwnerManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRegister = { navController.navigate(Cadastro()) },
                    onNavigateToEdit = { ownerId ->
                        navController.navigate(Cadastro(ownerId))
                    }
                )
            }
        }
    }
}
