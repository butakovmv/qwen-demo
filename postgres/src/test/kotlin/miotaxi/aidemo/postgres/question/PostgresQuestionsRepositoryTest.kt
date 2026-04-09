package miotaxi.aidemo.postgres.question

import kotlinx.coroutines.test.runTest
import miotaxi.aidemo.postgres.PostgresTestConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [PostgresTestConfig::class])
@ActiveProfiles("h2")
@ComponentScan
@EnableR2dbcRepositories
class PostgresQuestionsRepositoryTest {
    @Autowired
    private lateinit var questionsRepo: PostgresQuestionsRepository

    @Test
    fun `findAll returns questions from database`() =
        runTest {
            val questions = questionsRepo.findAll()

            assertEquals(2, questions.size)
            assertEquals("550e8400-e29b-41d4-a716-446655440000", questions[0].id)
            assertEquals("What is your name?", questions[0].text)
        }
}
