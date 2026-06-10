package miotaxi.aidemo.naturalLanguage

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import miotaxi.aidemo.langchain.NaturalLanguageCommandService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SendNaturalLanguageCommandOperationImplTest {
    private val service = mockk<NaturalLanguageCommandService>()
    private val objectMapper = ObjectMapper()
    private val operation = SendNaturalLanguageCommandOperationImpl(service, objectMapper)

    @Test
    fun `execute returns success when LLM returns valid JSON with success=true`() =
        runTest {
            every { service.interpret("any text") } returns """{"success":true,"result":"Вопросы получены"}"""
            val request = request("any text")

            val result = operation.execute(request)

            assertTrue(result.success)
            assertEquals("Вопросы получены", result.text)
        }

    @Test
    fun `execute returns failure when LLM returns valid JSON with success=false`() =
        runTest {
            every { service.interpret("any text") } returns
                """{"success":false,"result":"Неизвестная команда"}"""
            val request = request("any text")

            val result = operation.execute(request)

            assertFalse(result.success)
            assertEquals("Неизвестная команда", result.text)
        }

    @Test
    fun `execute returns failure when LLM throws`() =
        runTest {
            every { service.interpret("any text") } throws RuntimeException("Connection timeout")
            val request = request("any text")

            val result = operation.execute(request)

            assertFalse(result.success)
            assertTrue(result.text.contains("Connection timeout"))
        }

    @Test
    fun `execute treats ERROR prefix as failure when JSON is invalid`() =
        runTest {
            every { service.interpret("any text") } returns "ERROR: unsupported command"
            val request = request("any text")

            val result = operation.execute(request)

            assertFalse(result.success)
            assertEquals("unsupported command", result.text)
        }

    @Test
    fun `execute treats plain non-JSON response as success with raw text`() =
        runTest {
            every { service.interpret("any text") } returns "Вот список вопросов: ..."
            val request = request("any text")

            val result = operation.execute(request)

            assertTrue(result.success)
            assertEquals("Вот список вопросов: ...", result.text)
        }

    @Test
    fun `execute returns failure when LLM returns empty string`() =
        runTest {
            every { service.interpret("any text") } returns ""
            val request = request("any text")

            val result = operation.execute(request)

            assertFalse(result.success)
            assertTrue(result.text.contains("пустой"))
        }

    @Test
    fun `execute passes request text to service`() =
        runTest {
            val captured = mutableListOf<String>()
            every { service.interpret(capture(captured)) } returns """{"success":true,"result":"ok"}"""
            val request = request("Set timer for 5 minutes")

            operation.execute(request)

            assertEquals(1, captured.size)
            assertEquals("Set timer for 5 minutes", captured[0])
        }

    private fun request(text: String): SendNaturalLanguageCommandOperation.Request =
        object : SendNaturalLanguageCommandOperation.Request {
            override val text = text
        }
}
