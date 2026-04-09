package miotaxi.aidemo.answer

import kotlinx.coroutines.test.runTest
import miotaxi.aidemo.BasePostgresTest
import miotaxi.aidemo.question.QuestionEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class PostgresAnswersRepositoryTest : BasePostgresTest() {
    private lateinit var questions: List<QuestionEntity>

    @BeforeEach
    fun setUpFixture() {
        cleanup()
        questions = questionGenerator.createAndInsert(2)
    }

    @Autowired
    private lateinit var answersRepo: PostgresAnswersRepository

    @Test
    fun `saveAll persists answers to database`() =
        runTest {
            val testAnswers =
                questions.map { q ->
                    AnswerEntity(questionId = q.id, text = "Answer ${UUID.randomUUID().toString().substring(0, 8)}")
                }
            val repoAnswers =
                testAnswers.map { a ->
                    AnswersRepository.Answer(a.questionId, a.text)
                }

            val result = answersRepo.saveAll(repoAnswers)
            assertTrue(result)

            val stored = answersRepo.findAll()
            assertEquals(testAnswers.size, stored.size)
            assertEquals(testAnswers[0].questionId, stored[0].questionId)
            assertEquals(testAnswers[0].text, stored[0].text)
        }
}
