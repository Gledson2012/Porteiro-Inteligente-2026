package br.com.porteirointeligente.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.porteirointeligente.R
import br.com.porteirointeligente.databinding.ActivityHomeBinding
import br.com.porteirointeligente.ui.owner.OwnerDetailsActivity
import br.com.porteirointeligente.ui.owner.OwnerRegistrationActivity
import br.com.porteirointeligente.ui.scanner.ScannerActivity
import br.com.porteirointeligente.ui.settings.SettingsActivity
import br.com.porteirointeligente.ui.visit.VisitHistoryActivity
import br.com.porteirointeligente.ui.visit.VisitRegistrationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Tela inicial do aplicativo.
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val visitAdapter = VisitAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeState()

        // Identificação padrão.
        viewModel.configurarIdentificacao(
            condominio = getString(br.com.porteirointeligente.R.string.home_condominio_padrao),
            apartamento = getString(br.com.porteirointeligente.R.string.home_apartamento_padrao)
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerVisitas.apply {
            adapter = visitAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    private fun setupListeners() {
        binding.buttonRegistrarVisita.setOnClickListener {
            startActivity(Intent(this, VisitRegistrationActivity::class.java))
        }
        binding.buttonSouEntregador.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Já estamos na home
                    true
                }
                R.id.nav_visits -> {
                    startActivity(Intent(this, VisitHistoryActivity::class.java))
                    false
                }
                R.id.nav_qr -> {
                    val morador = viewModel.moradorCadastrado.value
                    if (morador != null) {
                        startActivity(Intent(this, OwnerDetailsActivity::class.java))
                    } else {
                        startActivity(Intent(this, OwnerRegistrationActivity::class.java))
                    }
                    false
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.condominio.collect { condominio ->
                        binding.textCondominio.text = condominio
                    }
                }
                launch {
                    viewModel.apartamento.collect { apartamento ->
                        binding.textApartamento.text = apartamento
                    }
                }
                launch {
                    viewModel.visitasRecentes.collect { visitas ->
                        binding.textQuantidadeVisitas.text = visitas.size.toString()
                        visitAdapter.submitList(visitas)
                        binding.textEmptyVisits.visibility = if (visitas.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.moradorCadastrado.collect { morador ->
                        if (morador != null) {
                            binding.buttonCadastrarMorador.visibility = View.GONE
                            binding.buttonVerQrCode.visibility = View.VISIBLE
                        } else {
                            binding.buttonCadastrarMorador.visibility = View.VISIBLE
                            binding.buttonVerQrCode.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}
