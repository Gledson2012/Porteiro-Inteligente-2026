package br.com.porteirointeligente.ui.owner

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.porteirointeligente.R
import br.com.porteirointeligente.databinding.ActivityOwnerRegistrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Tela de cadastro de novos moradores com campos expandidos e foto.
 */
@AndroidEntryPoint
class OwnerRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerRegistrationBinding
    private val viewModel: OwnerRegistrationViewModel by viewModels()
    private var selectedPhotoUri: Uri? = null
    private var ownerId: Long = 0L

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            binding.imageAvatar.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkEditMode()
        setupNavigation()
        setupListeners()
        observeEvents()
    }

    private fun checkEditMode() {
        val isEditMode = intent.getBooleanExtra("EXTRA_EDIT_MODE", false)
        if (isEditMode) {
            ownerId = intent.getLongExtra("EXTRA_ID", 0L)
            binding.textRegTitulo.text = getString(R.string.owner_details_button_edit)
            binding.editCondominio.setText(intent.getStringExtra("EXTRA_CONDOMINIO"))
            binding.editNome.setText(intent.getStringExtra("EXTRA_NAME"))
            binding.editEndereco.setText(intent.getStringExtra("EXTRA_ADDRESS"))
            binding.editCep.setText(intent.getStringExtra("EXTRA_CEP"))
            binding.editApartamento.setText(intent.getStringExtra("EXTRA_UNIT"))
            binding.editTelefone.setText(intent.getStringExtra("EXTRA_PHONE"))
            
            val photoUriString = intent.getStringExtra("EXTRA_PHOTO")
            if (photoUriString != null) {
                selectedPhotoUri = Uri.parse(photoUriString)
                binding.imageAvatar.setImageURI(selectedPhotoUri)
            }
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_qr
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_visits -> {
                    Toast.makeText(this, "Histórico em breve", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_qr -> true
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.imageAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.buttonSalvar.setOnClickListener {
            binding.layoutCondominio.error = null
            binding.layoutNome.error = null
            binding.layoutEndereco.error = null
            binding.layoutCep.error = null
            binding.layoutApartamento.error = null
            binding.layoutTelefone.error = null

            viewModel.registerOwner(
                id = ownerId,
                nome = binding.editNome.text.toString(),
                nomeCondominio = binding.editCondominio.text.toString(),
                endereco = binding.editEndereco.text.toString(),
                cep = binding.editCep.text.toString(),
                apartamento = binding.editApartamento.text.toString(),
                telefone = binding.editTelefone.text.toString(),
                photoUri = selectedPhotoUri?.toString()
            )
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationEvent.collect { event ->
                    when (event) {
                        is OwnerRegistrationViewModel.RegistrationUiEvent.Success -> {
                            Toast.makeText(this@OwnerRegistrationActivity, R.string.owner_reg_success, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is OwnerRegistrationViewModel.RegistrationUiEvent.ErrorFields -> {
                            showValidationErrors()
                        }
                        is OwnerRegistrationViewModel.RegistrationUiEvent.ErrorPhone -> {
                            binding.layoutTelefone.error = getString(R.string.owner_reg_error_phone)
                        }
                    }
                }
            }
        }
    }

    private fun showValidationErrors() {
        if (binding.editCondominio.text.isNullOrBlank()) binding.layoutCondominio.error = getString(R.string.owner_reg_error_fields)
        if (binding.editNome.text.isNullOrBlank()) binding.layoutNome.error = getString(R.string.owner_reg_error_fields)
        if (binding.editEndereco.text.isNullOrBlank()) binding.layoutEndereco.error = getString(R.string.owner_reg_error_fields)
        if (binding.editCep.text.isNullOrBlank()) binding.layoutCep.error = getString(R.string.owner_reg_error_fields)
        if (binding.editApartamento.text.isNullOrBlank()) binding.layoutApartamento.error = getString(R.string.owner_reg_error_fields)
        if (binding.editTelefone.text.isNullOrBlank()) binding.layoutTelefone.error = getString(R.string.owner_reg_error_fields)
        
        Toast.makeText(this, R.string.owner_reg_error_fields, Toast.LENGTH_SHORT).show()
    }
}
