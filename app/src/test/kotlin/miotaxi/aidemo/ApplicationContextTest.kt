package miotaxi.aidemo

import io.mockk.mockk
import miotaxi.aidemo.answer.AnswersRepository
import miotaxi.aidemo.question.QuestionsRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Primary
import org.springframework.test.context.TestPropertySource

@SpringBootTest(
    classes = [ApplicationContextTest.TestConfig::class],
)
@ComponentScan(
    basePackages = ["miotaxi.aidemo"],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [
                "miotaxi\\.aidemo\\.answer\\.PostgresAnswersRepository",
                "miotaxi\\.aidemo\\.answer\\.AnswersCrudRepository",
                "miotaxi\\.aidemo\\.question\\.PostgresQuestionsRepository",
                "miotaxi\\.aidemo\\.question\\.QuestionsCrudRepository",
                "miotaxi\\.aidemo\\.PostgresConfig",
            ],
        ),
    ],
)
@TestPropertySource(
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
    ],
)
class ApplicationContextTest {
    @Suppress("EmptyFunctionBlock")
    @Test
    fun `context loads successfully`() {}

    @TestConfiguration
    internal class TestConfig {
        @Bean
        @Primary
        internal fun answersRepository(): AnswersRepository = mockk()

        @Bean
        @Primary
        internal fun questionsRepository(): QuestionsRepository = mockk()
    }
}
