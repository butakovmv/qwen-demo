package miotaxi.aidemo.webapi

import io.mockk.mockk
import miotaxi.aidemo.operation.answer.SendAnswersOperation
import miotaxi.aidemo.operation.question.GetQuestionsOperation
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
internal class TestConfig {
    @Bean
    internal fun getQuestionsOperation(): GetQuestionsOperation = mockk()

    @Bean
    internal fun sendAnswersOperation(): SendAnswersOperation = mockk()
}
