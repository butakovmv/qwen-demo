package miotaxi.aidemo.webapi.controller

import io.mockk.mockk
import miotaxi.aidemo.operation.GetQuestionsOperation
import miotaxi.aidemo.operation.SendAnswersOperation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(useDefaultFilters = false)
internal class TestApplication

@TestConfiguration
internal class TestConfig {
    @Bean
    internal fun getQuestionsOperation(): GetQuestionsOperation = mockk()

    @Bean
    internal fun sendAnswersOperation(): SendAnswersOperation = mockk()

    @Bean
    internal fun getQuestionsController(getQuestionsOperation: GetQuestionsOperation) =
        GetQuestionsController(getQuestionsOperation)

    @Bean
    internal fun postAnswersController(sendAnswersOperation: SendAnswersOperation) =
        PostAnswersController(sendAnswersOperation)
}
