package br.com.porteirointeligente.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val shouldShowOnboarding by onboardingViewModel.shouldShowOnboarding.collectAsState()

    val startDestination = remember {
        if (shouldShowOnboarding) Onboarding else Splash
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
            SplashScreen(onSplashFinished = {
                when (authState) {
                    is AuthState.Authenticated -> {
                        navController.navigate(Home) {
                            popUpTo<Splash> { inclusive = true }
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        navController.navigate(Login) {
                            popUpTo<Splash> { inclusive = true }
                        }
                    }
                    is AuthState.Loading -> { /* Should not happen */ }
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
