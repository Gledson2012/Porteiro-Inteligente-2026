package br.com.porteirointeligente.ui.visit

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
import br.com.porteirointeligente.databinding.ActivityVisitHistoryBinding
import br.com.porteirointeligente.ui.owner.OwnerDetailsActivity
import br.com.porteirointeligente.ui.owner.OwnerRegistrationActivity
import br.com.porteirointeligente.ui.settings.SettingsActivity
import br.com.porteirointeligente.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VisitHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitHistoryBinding
    private val viewModel: VisitHistoryViewModel by viewModels()
    private val adapter = VisitHistoryAdapter { visit ->
        viewModel.registrarSaida(visit)
        Toast.makeText(this, R.string.visits_history_exit_success, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFilters()
        setupNavigation()
        observeState()
    }

    private fun setupRecyclerView() {
        binding.recyclerHistory.apply {
            adapter = this@VisitHistoryActivity.adapter
            layoutManager = LinearLayoutManager(this@VisitHistoryActivity)
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_all -> viewModel.setFilter(VisitHistoryViewModel.Filter.ALL)
                R.id.chip_active -> viewModel.setFilter(VisitHistoryViewModel.Filter.ACTIVE)
            }
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_visits
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_visits -> true
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
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visits.collect { visits ->
                    adapter.submitList(visits)
                    binding.textEmptyHistory.visibility = if (visits.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
