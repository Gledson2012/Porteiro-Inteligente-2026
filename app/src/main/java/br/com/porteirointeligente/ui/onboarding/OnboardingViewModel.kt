package br.com.porteirointeligente.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.util.OnboardingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingManager: OnboardingManager
) : ViewModel() {

    private val _shouldShowOnboarding = MutableStateFlow(true) // Default true para evitar race condition
    val shouldShowOnboarding: StateFlow<Boolean> = _shouldShowOnboarding

    init {
        viewModelScope.launch {
            _shouldShowOnboarding.value = onboardingManager.shouldShowOnboarding()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingManager.completeOnboarding()
        }
    }
}
