package br.com.porteirointeligente.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.porteirointeligente.ui.auth.LoginScreen
import br.com.porteirointeligente.ui.auth.RegistrationScreen
import br.com.porteirointeligente.ui.onboarding.OnboardingScreen
import br.com.porteirointeligente.ui.onboarding.OnboardingViewModel
import br.com.porteirointeligente.ui.splash.SplashScreen

@Composable
fun RootNavGraph(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        composable<Splash> {
            SplashScreen(onSplashFinished = {
                val destination = when (authState) {
                    is AuthState.Authenticated -> Home
                    is AuthState.Unauthenticated -> Login
                    is AuthState.Loading -> Splash
                }
                navController.navigate(destination) {
                    popUpTo<Splash> { inclusive = true }
                }
            })
        }

        composable<Login> {
            LoginScreen(navController = navController)
        }

        composable<Register> {
            RegistrationScreen(navController = navController)
        }

        composable<Home> {
            MainScreenNavGraph()
        }
    }
}
