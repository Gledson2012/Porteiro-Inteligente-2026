package br.com.porteirointeligente.util

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente para consulta de CEP via API ViaCEP.
 * Retorna o endereço completo a partir do CEP informado.
 */
object ViaCepClient {

    data class CepResult(
        val cep: String,
        val logradouro: String,
        val bairro: String,
        val cidade: String,
        val estado: String,
        val erro: Boolean = false
    ) {
        val enderecoCompleto: String
            get() = "$logradouro, $bairro, $cidade - $estado"
    }

    /**
     * Consulta um CEP na API ViaCEP.
     * @param cep CEP com 8 dígitos (apenas números)
     * @return CepResult com os dados do endereço, ou com erro=true se não encontrado
     */
    suspend fun consultar(cep: String): CepResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://viacep.com.br/ws/$cep/json/")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val json = JsonParser.parseString(response).asJsonObject

                if (json.has("erro") && json.get("erro").asBoolean) {
                    return@withContext CepResult(cep = cep, logradouro = "", bairro = "", cidade = "", estado = "", erro = true)
                }

                CepResult(
                    cep = json.get("cep")?.asString?.replace("-", "") ?: cep,
                    logradouro = json.get("logradouro")?.asString ?: "",
                    bairro = json.get("bairro")?.asString ?: "",
                    cidade = json.get("localidade")?.asString ?: "",
                    estado = json.get("uf")?.asString ?: "",
                    erro = false
                )
            } catch (e: Exception) {
                CepResult(cep = cep, logradouro = "", bairro = "", cidade = "", estado = "", erro = true)
            }
        }
    }
}
