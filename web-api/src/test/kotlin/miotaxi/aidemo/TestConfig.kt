package miotaxi.aidemo

import io.mockk.mockk
import miotaxi.aidemo.answer.AnswersRepository
import miotaxi.aidemo.answer.SendAnswersOperation
import miotaxi.aidemo.question.GetQuestionsOperation
import miotaxi.aidemo.question.QuestionsRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
internal class TestConfig {
    @Bean
    @Primary
    internal fun getQuestionsOperation(): GetQuestionsOperation = mockk()

    @Bean
    @Primary
    internal fun sendAnswersOperation(): SendAnswersOperation = mockk()

    @Bean
    @Primary
    internal fun answersRepository(): AnswersRepository = mockk()

    @Bean
    @Primary
    internal fun questionsRepository(): QuestionsRepository = mockk()
}
