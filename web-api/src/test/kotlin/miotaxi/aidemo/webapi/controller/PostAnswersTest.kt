package miotaxi.aidemo.webapi.controller

import io.mockk.coEvery
import io.mockk.slot
import miotaxi.aidemo.operation.SendAnswersOperation
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.json.JsonCompareMode.STRICT
import org.springframework.test.web.reactive.server.WebTestClient

internal class PostAnswersTest : BaseWebTest() {
    @Autowired
    private lateinit var sendAnswersOperation: SendAnswersOperation

    override fun sut(): WebTestClient.ResponseSpec =
        webTestClient.post()
            .uri("/api/v1/answers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(answersRequest("q1" to "My name is John"))
            .exchange()

    @Test
    fun `returns 200 with success=true`() {
        val captured = slot<SendAnswersOperation.Request>()
        coEvery { sendAnswersOperation.execute(capture(captured)) } returns SuccessTestResponse

        sut()
            .expectOk()
            .json("""{"success":true}""", STRICT)
    }
}

private object SuccessTestResponse : SendAnswersOperation.Response {
    override val success = true
}

private fun answersRequest(vararg answers: Pair<String, String>): String {
    val items =
        answers.joinToString(",") { (id, text) ->
            """{"id":"$id","text":"$text"}"""
        }
    return """{"answers":[$items]}"""
}
