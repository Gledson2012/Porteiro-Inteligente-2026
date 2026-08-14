package br.com.porteirointeligente.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.porteirointeligente.ui.auth.LoginScreen
import br.com.porteirointeligente.ui.auth.RegistrationScreen
import br.com.porteirointeligente.ui.onboarding.OnboardingScreen
import br.com.porteirointeligente.ui.onboarding.OnboardingViewModel
import br.com.porteirointeligente.ui.splash.SplashScreen
import br.com.porteirointeligente.util.AppUpdateChecker
import br.com.porteirointeligente.util.AppUpdateInfo
import androidx.navigation.NavDestination.Companion.hasRoute

@Composable
fun RootNavGraph(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val shouldShowOnboarding by onboardingViewModel.shouldShowOnboarding.collectAsState()
    var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var splashFinished by remember { mutableStateOf(false) }

    val startDestination = remember {
        if (shouldShowOnboarding) Onboarding else Splash
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated &&
            navController.currentDestination?.hasRoute<Home>() == true
        ) {
            navController.navigate(Login) {
                popUpTo<Home> { inclusive = true }
            }
        }
    }

    // O ViewModel inicia com true para evitar uma tela vazia enquanto o DataStore
    // é lido. Se o onboarding já tiver sido concluído, remove a tela provisória
    // assim que o valor persistido estiver disponível.
    LaunchedEffect(shouldShowOnboarding) {
        if (!shouldShowOnboarding &&
            navController.currentDestination?.hasRoute<Onboarding>() == true
        ) {
            navController.navigate(Splash) {
                popUpTo<Onboarding> { inclusive = true }
            }
        }
    }

    // A autenticação e a leitura do DataStore podem demorar mais que a animação
    // da Splash. Aguarda os dois estados antes de navegar, evitando ficar preso
    // na tela inicial quando o callback da Splash ocorrer enquanto auth ainda
    // estiver em Loading.
    LaunchedEffect(authState, splashFinished) {
        if (!splashFinished || authState is AuthState.Loading) return@LaunchedEffect
        if (navController.currentDestination?.hasRoute<Splash>() != true) return@LaunchedEffect

        when (authState) {
            is AuthState.Authenticated -> navController.navigate(Home) {
                popUpTo<Splash> { inclusive = true }
            }
            is AuthState.Unauthenticated -> navController.navigate(Login) {
                popUpTo<Splash> { inclusive = true }
            }
            is AuthState.Loading -> Unit
        }
    }

    LaunchedEffect(Unit) {
        availableUpdate = AppUpdateChecker.check()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Onboarding> {
            OnboardingScreen(
                onOnboardingFinished = {
                    onboardingViewModel.completeOnboarding()
                    navController.navigate(Splash) {
                        popUpTo<Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<Splash> {
            SplashScreen(onSplashFinished = { splashFinished = true })
        }

        composable<Login> {
            LoginScreen(navController = navController)
        }

        composable<Register> {
            RegistrationScreen(navController = navController)
        }

        composable<Home> {
            MainScreenNavGraph(onLogout = authViewModel::logout)
        }
    }

    availableUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = { availableUpdate = null },
            title = { Text("Atualização disponível") },
            text = {
                Text(
                    buildString {
                        append("Uma nova versão (${update.versionName}) está disponível.")
                        if (update.releaseNotes.isNotBlank()) {
                            append("\n\n")
                            append(update.releaseNotes)
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        availableUpdate = null
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl))
                        )
                    }
                ) {
                    Text("Baixar atualização")
                }
            },
            dismissButton = {
                TextButton(onClick = { availableUpdate = null }) {
                    Text("Agora não")
                }
            }
        )
    }
}
