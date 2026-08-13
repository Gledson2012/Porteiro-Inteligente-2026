package br.com.porteirointeligente.util

/** Base64 URL-safe sem dependência de APIs Android, para funcionar também nos testes JVM. */
object Base64Url {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encode(input: ByteArray): String {
        val output = StringBuilder((input.size + 2) / 3 * 4)
        var index = 0
        while (index < input.size) {
            val first = input[index++].toInt() and 0xff
            val hasSecond = index < input.size
            val second = if (hasSecond) input[index++].toInt() and 0xff else 0
            val hasThird = index < input.size
            val third = if (hasThird) input[index++].toInt() and 0xff else 0

            output.append(ALPHABET[first ushr 2])
            output.append(ALPHABET[((first and 0x03) shl 4) or (second ushr 4)])
            if (hasSecond) output.append(ALPHABET[((second and 0x0f) shl 2) or (third ushr 6)])
            if (hasThird) output.append(ALPHABET[third and 0x3f])
        }
        return output.toString().replace('+', '-').replace('/', '_')
    }

    fun decode(value: String): ByteArray {
        val normalized = value.replace('-', '+').replace('_', '/')
        val output = ArrayList<Byte>(normalized.length * 3 / 4)
        var buffer = 0
        var bits = 0

        for (character in normalized) {
            if (character == '=') break
            val digit = ALPHABET.indexOf(character)
            require(digit >= 0) { "Base64 inválido" }
            buffer = (buffer shl 6) or digit
            bits += 6
            if (bits >= 8) {
                bits -= 8
                output += ((buffer ushr bits) and 0xff).toByte()
            }
        }
        return output.toByteArray()
    }
}
