package br.com.porteirointeligente.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PhotoSaver {

    private const val PHOTO_DIR = "profile_photos"

    fun savePhotoToInternalStorage(context: Context, photoUri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                val dir = File(context.filesDir, PHOTO_DIR)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "${UUID.randomUUID()}.jpg")
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
                file.toURI().toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePhoto(context: Context, photoPath: String) {
        try {
            val file = File(Uri.parse(photoPath).path ?: return)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
