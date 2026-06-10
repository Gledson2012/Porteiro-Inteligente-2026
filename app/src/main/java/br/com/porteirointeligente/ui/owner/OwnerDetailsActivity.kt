package br.com.porteirointeligente.ui.owner

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.porteirointeligente.R
import br.com.porteirointeligente.databinding.ActivityOwnerDetailsBinding
import br.com.porteirointeligente.ui.owner.OwnerRegistrationActivity
import br.com.porteirointeligente.ui.settings.SettingsActivity
import br.com.porteirointeligente.ui.visit.VisitHistoryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Tela que exibe os detalhes expandidos do morador e o QR Code.
 */
@AndroidEntryPoint
class OwnerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerDetailsBinding
    private val viewModel: OwnerDetailsViewModel by viewModels()
    private var currentQrCode: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupListeners()
        viewModel.loadOwner()
        observeState()
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
                    startActivity(Intent(this, VisitHistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_qr -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.buttonShare.setOnClickListener {
            currentQrCode?.let { shareQrCode(it) }
        }
        binding.buttonSave.setOnClickListener {
            currentQrCode?.let { saveQrCodeToGallery(it) }
        }
        binding.buttonPdf.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is OwnerDetailsViewModel.OwnerDetailsUiState.Success) {
                generatePdf(state)
            }
        }
        binding.buttonEdit.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is OwnerDetailsViewModel.OwnerDetailsUiState.Success) {
                val intent = Intent(this, OwnerRegistrationActivity::class.java).apply {
                    putExtra("EXTRA_EDIT_MODE", true)
                    putExtra("EXTRA_ID", state.owner.id)
                    putExtra("EXTRA_CONDOMINIO", state.owner.nomeCondominio)
                    putExtra("EXTRA_NAME", state.owner.nome)
                    putExtra("EXTRA_ADDRESS", state.owner.endereco)
                    putExtra("EXTRA_CEP", state.owner.cep)
                    putExtra("EXTRA_UNIT", state.owner.apartamento)
                    putExtra("EXTRA_PHONE", state.owner.telefone)
                    putExtra("EXTRA_PHOTO", state.owner.photoUri)
                }
                startActivity(intent)
            }
        }
        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.owner_details_button_delete)
            .setMessage(R.string.owner_details_delete_confirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteOwner()
                Toast.makeText(this, R.string.owner_details_delete_success, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is OwnerDetailsViewModel.OwnerDetailsUiState.Success -> {
                            val owner = state.owner
                            binding.textNome.text = owner.nome
                            binding.textApartamento.text = "${getString(R.string.home_label_apartamento)} ${owner.apartamento}"
                            binding.textTelefone.text = "${getString(R.string.owner_reg_hint_telefone)} ${owner.telefone}"
                            binding.textEnderecoCompleto.text = "${owner.endereco}, ${owner.apartamento}"
                            
                            // Exibe a foto de perfil se existir
                            owner.photoUri?.let {
                                binding.imageAvatar.setImageURI(Uri.parse(it))
                            } ?: run {
                                binding.imageAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
                            }
                            
                            binding.imageQrCode.setImageBitmap(state.qrCode)
                            currentQrCode = state.qrCode
                        }
                        is OwnerDetailsViewModel.OwnerDetailsUiState.Empty -> {
                            finish()
                        }
                        is OwnerDetailsViewModel.OwnerDetailsUiState.Loading -> {
                        }
                    }
                }
            }
        }
    }

    private fun shareQrCode(bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cachePath = File(cacheDir, "images")
                cachePath.mkdirs()
                val stream = FileOutputStream("$cachePath/qr_code.png")
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                val imagePath = File(cacheDir, "images")
                val newFile = File(imagePath, "qr_code.png")
                val contentUri = FileProvider.getUriForFile(this@OwnerDetailsActivity, "${packageName}.fileprovider", newFile)

                if (contentUri != null) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(contentUri, contentResolver.getType(contentUri))
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/png"
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.owner_details_share_title)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveQrCodeToGallery(bitmap: Bitmap) {
        val filename = "QRCode_Porteiro_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(this, R.string.owner_details_save_success, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, R.string.owner_details_save_error, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun generatePdf(state: OwnerDetailsViewModel.OwnerDetailsUiState.Success) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            paint.color = Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText(getString(R.string.owner_details_title), 50f, 50f, paint)

            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("${getString(R.string.owner_details_label_nome)} ${state.owner.nome}", 50f, 100f, paint)
            canvas.drawText("${getString(R.string.owner_details_label_apartamento)} ${state.owner.apartamento}", 50f, 130f, paint)
            
            state.qrCode?.let {
                val scaledQr = Bitmap.createScaledBitmap(it, 300, 300, true)
                canvas.drawBitmap(scaledQr, 147f, 200f, paint)
            }

            paint.textSize = 12f
            paint.color = Color.GRAY
            canvas.drawText(getString(R.string.owner_details_qr_desc), 50f, 550f, paint)

            pdfDocument.finishPage(page)

            val fileName = "QRCode_Porteiro_${state.owner.apartamento}.pdf"
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            pdfDocument.writeTo(outputStream)
                        }
                    }
                } else {
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                    pdfDocument.writeTo(FileOutputStream(file))
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OwnerDetailsActivity, R.string.owner_details_pdf_success, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OwnerDetailsActivity, R.string.owner_details_pdf_error, Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            } finally {
                pdfDocument.close()
            }
        }
    }
}
