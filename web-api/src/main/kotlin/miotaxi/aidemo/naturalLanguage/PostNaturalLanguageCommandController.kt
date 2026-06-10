package miotaxi.aidemo.naturalLanguage

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
@Tag(name = "Natural Language", description = "API для команд на естественном языке")
internal class PostNaturalLanguageCommandController(
    private val sendNaturalLanguageCommandOperation: SendNaturalLanguageCommandOperation,
) {
    @PostMapping("/natural-language/commands")
    @Operation(
        summary = "Отправить команду на естественном языке",
        description = "Принимает текст команды и возвращает результат её обработки",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Успешный ответ"),
    )
    suspend fun sendCommand(
        @RequestBody request: NaturalLanguageCommandRequest,
    ): NaturalLanguageCommandResponse =
        sendNaturalLanguageCommandOperation
            .execute(request.toOperationRequest())
            .toResponse()
}

internal data class NaturalLanguageCommandRequest(
    val text: String,
)

internal data class NaturalLanguageCommandResponse(
    val success: Boolean,
    val text: String,
)

private fun NaturalLanguageCommandRequest.toOperationRequest(): SendNaturalLanguageCommandOperation.Request =
    object : SendNaturalLanguageCommandOperation.Request {
        override val text = this@toOperationRequest.text
    }

private fun SendNaturalLanguageCommandOperation.Response.toResponse(): NaturalLanguageCommandResponse =
    NaturalLanguageCommandResponse(success = success, text = text)
