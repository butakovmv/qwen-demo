package miotaxi.aidemo.naturalLanguage

import io.mockk.coEvery
import io.mockk.slot
import miotaxi.aidemo.BaseWebTest
import miotaxi.aidemo.TestConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.json.JsonCompareMode.STRICT
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [PostNaturalLanguageCommandController::class])
@Import(TestConfig::class)
internal class PostNaturalLanguageCommandTest : BaseWebTest() {
    @Autowired
    private lateinit var sendNaturalLanguageCommandOperation: SendNaturalLanguageCommandOperation

    override fun sut(): WebTestClient.ResponseSpec =
        webTestClient.post()
            .uri("/api/v1/natural-language/commands")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"text":"Hello, world!"}""")
            .exchange()

    @Test
    fun `returns 200 with success=true and response text`() {
        val captured = slot<SendNaturalLanguageCommandOperation.Request>()
        coEvery { sendNaturalLanguageCommandOperation.execute(capture(captured)) } returns
            successTestResponse("Command processed")

        sut()
            .expectOk()
            .json("""{"success":true,"text":"Command processed"}""", STRICT)
    }

    @Test
    fun `passes request text to operation`() {
        val captured = slot<SendNaturalLanguageCommandOperation.Request>()
        coEvery { sendNaturalLanguageCommandOperation.execute(capture(captured)) } returns
            successTestResponse("ok")

        sut().expectOk()

        assert(captured.isCaptured)
        assert(captured.captured.text == "Hello, world!")
    }

    @Test
    fun `returns 200 with success=false on error response`() {
        coEvery { sendNaturalLanguageCommandOperation.execute(any()) } returns
            errorTestResponse("Unknown command")

        sut()
            .expectOk()
            .json("""{"success":false,"text":"Unknown command"}""", STRICT)
    }
}

private fun successTestResponse(text: String): SendNaturalLanguageCommandOperation.Response =
    object : SendNaturalLanguageCommandOperation.Response {
        override val success = true
        override val text = text
    }

private fun errorTestResponse(text: String): SendNaturalLanguageCommandOperation.Response =
    object : SendNaturalLanguageCommandOperation.Response {
        override val success = false
        override val text = text
    }
