package miotaxi.aidemo.answer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Answers", description = "API для ответов")
internal class PostAnswersController(
    private val sendAnswersOperation: SendAnswersOperation,
) {
    @PostMapping("/answers")
    @Operation(summary = "Отправить ответы", description = "Принимает список ответов и возвращает результат обработки")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Успешный ответ"),
    )
    suspend fun sendAnswers(
        @RequestBody request: AnswersRequest,
    ): AnswersResponse = sendAnswersOperation.execute(request.toOperationRequest()).toResponse()
}

internal data class AnswersRequest(
    val answers: List<AnswerDto>,
)

internal data class AnswerDto(
    val id: String,
    val text: String,
)

internal data class AnswersResponse(
    val success: Boolean,
)

private fun AnswersRequest.toOperationRequest(): SendAnswersOperation.Request {
    val dtoAnswers = answers
    return object : SendAnswersOperation.Request {
        override val answers =
            dtoAnswers.map { a ->
                SendAnswersOperation.Answer(a.id, a.text)
            }
    }
}

private fun SendAnswersOperation.Response.toResponse(): AnswersResponse = AnswersResponse(success)
