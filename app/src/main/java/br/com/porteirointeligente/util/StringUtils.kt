package br.com.porteirointeligente.util

/**
 * Utilitários compartilhados de formatação de strings para o app.
 */
object StringUtils {

    /**
     * Formata um número de telefone para exibição amigável.
     * Ex: 5511999998888 -> "(11) 99999-8888"
     */
    fun formatPhone(raw: String): String {
        val clean = raw.replace(Regex("[^0-9]"), "")
        return when {
            clean.length >= 11 -> {
                "(${clean.substring(0, 2)}) ${clean.substring(2, 7)}-${clean.substring(7, 11)}"
            }
            clean.length == 10 -> {
                "(${clean.substring(0, 2)}) ${clean.substring(2, 6)}-${clean.substring(6, 10)}"
            }
            else -> clean
        }
    }

    /**
     * Formata um CEP para exibição amigável.
     * Ex: 12345678 -> "12345-678"
     */
    fun formatCep(raw: String): String {
        val clean = raw.replace(Regex("[^0-9]"), "")
        return if (clean.length == 8) {
            "${clean.substring(0, 5)}-${clean.substring(5)}"
        } else {
            clean
        }
    }

    /**
     * Aplica máscara de telefone em tempo real para inputs.
     */
    fun maskPhone(input: String): String {
        val clean = input.replace(Regex("[^0-9]"), "")
        if (clean.length <= 11) {
            return when {
                clean.length > 7 -> {
                    val ddd = clean.take(2)
                    val firstPart = clean.substring(2, clean.length - 4)
                    val secondPart = clean.substring(clean.length - 4)
                    "($ddd) $firstPart-$secondPart"
                }
                clean.length > 2 -> {
                    val ddd = clean.take(2)
                    val firstPart = clean.substring(2)
                    "($ddd) $firstPart"
                }
                else -> clean
            }
        }
        return input
    }
}
