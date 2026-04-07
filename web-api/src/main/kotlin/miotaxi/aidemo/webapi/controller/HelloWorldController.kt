package miotaxi.aidemo.webapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import miotaxi.aidemo.operation.HelloWorldOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Hello World", description = "API для приветствия")
internal class HelloWorldController(
    private val helloWorldOperation: HelloWorldOperation,
) {
    @GetMapping("/hello-world")
    @Operation(summary = "Получить приветствие", description = "Возвращает сообщение с приветствием")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Успешный ответ"),
    )
    suspend fun helloWorld(): HelloWorldOperation.Response {
        return helloWorldOperation.execute()
    }
}
