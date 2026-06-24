package br.com.porteirointeligente.util

import org.junit.Test

class StringUtilsTest {

    @Test
    fun `formatPhone with 11 digits should format correctly`() {
        // 11 dígitos: DDD(2) + número(9) -> (XX) XXXXX-XXXX
        val result = StringUtils.formatPhone("11999998888")
        assert(result == "(11) 99999-8888") { "Expected (11) 99999-8888 but got $result" }
    }

    @Test
    fun `formatPhone with country code 55 should format using first 2 as DDD`() {
        // 13 dígitos com código de país 55
        val result = StringUtils.formatPhone("5511999998888")
        assert(result == "(55) 11999-9888") { "Expected (55) 11999-9888 but got $result" }
    }

    @Test
    fun `formatPhone with 10 digits should format without 9th digit`() {
        val result = StringUtils.formatPhone("1199998888")
        assert(result == "(11) 9999-8888") { "Expected (11) 9999-8888 but got $result" }
    }

    @Test
    fun `formatPhone with less than 10 digits should return raw`() {
        val result = StringUtils.formatPhone("12345")
        assert(result == "12345") { "Expected 12345 but got $result" }
    }

    @Test
    fun `formatCep with 8 digits should format correctly`() {
        val result = StringUtils.formatCep("12345678")
        assert(result == "12345-678") { "Expected 12345-678 but got $result" }
    }

    @Test
    fun `formatCep with invalid length should return raw`() {
        val result = StringUtils.formatCep("1234")
        assert(result == "1234") { "Expected 1234 but got $result" }
    }

    @Test
    fun `maskPhone with 11 digits should format DDD + 9-digit number`() {
        val result = StringUtils.maskPhone("11999998888")
        assert(result == "(11) 99999-8888") { "Expected (11) 99999-8888 but got $result" }
    }

    @Test
    fun `maskPhone with partial input should format progressively`() {
        val result = StringUtils.maskPhone("5511")
        assert(result == "(55) 11") { "Expected (55) 11 but got $result" }
    }

    @Test
    fun `maskPhone with 10 digits should format DDD + 8-digit number`() {
        val result = StringUtils.maskPhone("1199998888")
        assert(result == "(11) 9999-8888") { "Expected (11) 9999-8888 but got $result" }
    }
}
