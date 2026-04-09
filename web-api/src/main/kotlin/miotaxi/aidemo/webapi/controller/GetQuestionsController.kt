package miotaxi.aidemo.webapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import miotaxi.aidemo.operation.GetQuestionsOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Questions", description = "API для вопросов")
internal class GetQuestionsController(
    private val getQuestionsOperation: GetQuestionsOperation,
) {
    @GetMapping("/questions")
    @Operation(summary = "Получить список вопросов", description = "Возвращает список вопросов с id и text")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Успешный ответ"),
    )
    suspend fun getQuestions(): QuestionsResponse = getQuestionsOperation.execute().toResponse()
}

internal data class QuestionsResponse(
    val questions: List<QuestionDto>,
)

internal data class QuestionDto(
    val id: String,
    val text: String,
)

private fun GetQuestionsOperation.Response.toResponse(): QuestionsResponse =
    QuestionsResponse(
        questions = questions.map { q -> QuestionDto(q.id, q.text) },
    )
