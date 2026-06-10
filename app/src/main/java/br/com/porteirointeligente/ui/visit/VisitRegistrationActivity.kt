package br.com.porteirointeligente.ui.visit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.porteirointeligente.R
import br.com.porteirointeligente.databinding.ActivityVisitRegistrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Tela de registro de novas visitas com interface e validação melhoradas.
 */
@AndroidEntryPoint
class VisitRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitRegistrationBinding
    private val viewModel: VisitRegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeEvents()
    }

    private fun setupListeners() {
        binding.buttonRegistrar.setOnClickListener {
            // Limpa erros anteriores
            binding.layoutNome.error = null
            binding.layoutApartamento.error = null

            viewModel.registrarVisita(
                nome = binding.editNome.text.toString(),
                documento = binding.editDocumento.text.toString(),
                apartamento = binding.editApartamento.text.toString(),
                telefone = binding.editTelefone.text.toString(),
                motivo = binding.editMotivo.text.toString()
            )
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is VisitRegistrationViewModel.VisitUiEvent.Success -> {
                            Toast.makeText(this@VisitRegistrationActivity, R.string.visit_reg_success, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is VisitRegistrationViewModel.VisitUiEvent.ErrorFields -> {
                            showValidationErrors()
                        }
                    }
                }
            }
        }
    }

    private fun showValidationErrors() {
        if (binding.editNome.text.isNullOrBlank()) {
            binding.layoutNome.error = getString(R.string.visit_reg_error_fields)
        }
        if (binding.editApartamento.text.isNullOrBlank()) {
            binding.layoutApartamento.error = getString(R.string.visit_reg_error_fields)
        }
        Toast.makeText(this, R.string.visit_reg_error_generic, Toast.LENGTH_SHORT).show()
    }
}
