package miotaxi.aidemo.postgres.answer

import kotlinx.coroutines.test.runTest
import miotaxi.aidemo.operation.answer.AnswersRepository
import miotaxi.aidemo.postgres.PostgresTestConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [PostgresTestConfig::class])
@ActiveProfiles("h2")
@TestPropertySource(
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
    ],
)
@ComponentScan
@EnableR2dbcRepositories
class PostgresAnswersRepositoryTest {
    @Autowired
    private lateinit var answersRepo: PostgresAnswersRepository

    @Autowired
    private lateinit var dbClient: DatabaseClient

    @BeforeEach
    fun clearAnswers() =
        runTest {
            dbClient.sql("DELETE FROM answers").then().block()
        }

    @Test
    fun `saveAll persists answers to database`() =
        runTest {
            val answers =
                listOf(
                    AnswersRepository.Answer("550e8400-e29b-41d4-a716-446655440000", "Arthur"),
                    AnswersRepository.Answer("6ba7b810-9dad-11d1-80b4-00c04fd430c8", "To seek the Holy Grail"),
                )

            val result = answersRepo.saveAll(answers)
            assertTrue(result)

            val stored = answersRepo.findAll()
            assertEquals(2, stored.size)
            assertEquals("Arthur", stored[0].text)
        }
}
