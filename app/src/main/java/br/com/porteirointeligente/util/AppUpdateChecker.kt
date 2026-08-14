package br.com.porteirointeligente.util

import br.com.porteirointeligente.BuildConfig
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object AppUpdateChecker {
    private const val VERSION_URL =
        "https://porteiro-inteligente-2026.vercel.app/app-version.json"

    suspend fun check(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL("$VERSION_URL?installed=${BuildConfig.VERSION_CODE}")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                useCaches = false
                setRequestProperty("Cache-Control", "no-cache")
            }

            try {
                if (connection.responseCode !in 200..299) return@withContext null

                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JsonParser.parseString(json).asJsonObject
                val versionCode = root.get("versionCode")?.asInt ?: return@withContext null
                val versionName = root.get("versionName")?.asString ?: return@withContext null
                val downloadUrl = root.get("downloadUrl")?.asString ?: return@withContext null
                val releaseNotes = root.get("releaseNotes")?.asString.orEmpty()

                if (versionCode <= BuildConfig.VERSION_CODE ||
                    !downloadUrl.startsWith("https://") ||
                    versionName.isBlank()
                ) {
                    return@withContext null
                }

                AppUpdateInfo(versionCode, versionName, downloadUrl, releaseNotes)
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            // A falha de rede nunca impede o uso offline do aplicativo.
            null
        }
    }
}
