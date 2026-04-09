package miotaxi.aidemo.webapi

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@Import(TestConfig::class)
internal abstract class BaseWebTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    protected abstract fun sut(): WebTestClient.ResponseSpec

    @Test
    fun `returns 500 when operation throws`() {
        sut().expectStatus().is5xxServerError
    }

    protected fun WebTestClient.ResponseSpec.expectOk(): WebTestClient.BodyContentSpec =
        expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
}
