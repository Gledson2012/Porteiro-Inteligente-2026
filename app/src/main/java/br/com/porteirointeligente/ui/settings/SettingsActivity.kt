package br.com.porteirointeligente.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.porteirointeligente.R
import br.com.porteirointeligente.databinding.ActivitySettingsBinding
import br.com.porteirointeligente.ui.home.HomeActivity
import br.com.porteirointeligente.ui.owner.OwnerDetailsActivity
import br.com.porteirointeligente.ui.owner.OwnerRegistrationActivity
import br.com.porteirointeligente.ui.visit.VisitHistoryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupListeners()
        observeState()
        updateThemeSelection()
    }

    private fun updateThemeSelection() {
        val mode = AppCompatDelegate.getDefaultNightMode()
        when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.toggleTheme.check(R.id.btn_theme_light)
            AppCompatDelegate.MODE_NIGHT_YES -> binding.toggleTheme.check(R.id.btn_theme_dark)
            else -> binding.toggleTheme.check(R.id.btn_theme_system)
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_visits -> {
                    startActivity(Intent(this, VisitHistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_qr -> {
                    val morador = viewModel.owner.value
                    if (morador != null) {
                        startActivity(Intent(this, OwnerDetailsActivity::class.java))
                    } else {
                        startActivity(Intent(this, OwnerRegistrationActivity::class.java))
                    }
                    finish()
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_theme_light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    R.id.btn_theme_dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    R.id.btn_theme_system -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }

        binding.buttonSaveSettings.setOnClickListener {
            val isOffline = binding.switchOffline.isChecked
            val message = binding.editOfflineMessage.text.toString()
            
            val durationMillis = when (binding.toggleDuration.checkedButtonId) {
                R.id.btn_2h -> TimeUnit.HOURS.toMillis(2)
                R.id.btn_8h -> TimeUnit.HOURS.toMillis(8)
                R.id.btn_1w -> TimeUnit.DAYS.toMillis(7)
                R.id.btn_forever -> null
                else -> null
            }

            viewModel.updateOfflineStatus(isOffline, message, durationMillis)
            Toast.makeText(this, R.string.settings_save_success, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.owner.collect { owner ->
                    owner?.let {
                        binding.switchOffline.isChecked = it.isOffline
                        binding.editOfflineMessage.setText(it.offlineMessage)
                        
                        // Seleciona o botão de duração baseado no tempo restante (simplificado)
                        if (it.offlineUntil == null) {
                            binding.toggleDuration.check(R.id.btn_forever)
                        } else {
                            // Por simplificação, não vamos recalcular qual era o botão exato, 
                            // apenas manter o estado persistido.
                        }
                    }
                }
            }
        }
    }
}
