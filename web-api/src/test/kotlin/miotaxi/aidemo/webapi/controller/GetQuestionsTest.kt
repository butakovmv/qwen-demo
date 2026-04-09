package miotaxi.aidemo.webapi.controller

import io.mockk.coEvery
import miotaxi.aidemo.operation.GetQuestionsOperation
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient

internal class GetQuestionsTest : BaseWebTest() {
    @Autowired
    private lateinit var getQuestionsOperation: GetQuestionsOperation

    override fun sut(): WebTestClient.ResponseSpec =
        webTestClient.get()
            .uri("/api/v1/questions")
            .exchange()

    @Test
    fun `returns 200 with list of questions`() {
        coEvery { getQuestionsOperation.execute() } returns
            questionsResponse(
                "q1" to "What is your name?",
                "q2" to "What is your quest?",
            )

        sut()
            .expectOk()
            .jsonPath("$.questions.length()").isEqualTo(2)
            .jsonPath("$.questions[0].id").isEqualTo("q1")
            .jsonPath("$.questions[0].text").isEqualTo("What is your name?")
            .jsonPath("$.questions[1].id").isEqualTo("q2")
            .jsonPath("$.questions[1].text").isEqualTo("What is your quest?")
    }

    @Test
    fun `returns 200 with empty list when no questions`() {
        coEvery { getQuestionsOperation.execute() } returns questionsResponse()

        sut()
            .expectOk()
            .jsonPath("$.questions.length()").isEqualTo(0)
    }
}

private fun questionsResponse(vararg questions: Pair<String, String>): GetQuestionsOperation.Response {
    val list =
        questions.map { (id, text) ->
            GetQuestionsOperation.Question(id, text)
        }
    return object : GetQuestionsOperation.Response {
        override val questions = list
    }
}
