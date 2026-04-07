package miotaxi.aidemo.webapi.controller

import io.mockk.coEvery
import miotaxi.aidemo.operation.HelloWorldOperation
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

private fun helloResponse(message: String) =
    object : HelloWorldOperation.Response {
        override val message: String = message
    }

@WebFluxTest(HelloWorldController::class)
@Import(HelloWorldController::class)
class HelloWorldControllerTest {
    @MockitoBean
    private lateinit var helloWorldOperation: HelloWorldOperation

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `helloWorld — returns 200 with message`() {
        coEvery { helloWorldOperation.execute() } returns helloResponse("Hello, World!")

        webTestClient.get()
            .uri("/api/v1/hello-world")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Hello, World!")
    }

    @Test
    fun `helloWorld — returns empty message when operation returns empty`() {
        coEvery { helloWorldOperation.execute() } returns helloResponse("")

        webTestClient.get()
            .uri("/api/v1/hello-world")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("")
    }

    @Test
    fun `helloWorld — returns 500 when operation throws`() {
        coEvery { helloWorldOperation.execute() } throws RuntimeException("Operation error")

        webTestClient.get()
            .uri("/api/v1/hello-world")
            .exchange()
            .expectStatus().is5xxServerError
    }

    @Test
    fun `helloWorld — returns 404 for unknown path`() {
        webTestClient.get()
            .uri("/api/v1/unknown")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `helloWorld — returns 404 for root path`() {
        webTestClient.get()
            .uri("/")
            .exchange()
            .expectStatus().isNotFound
    }
}
