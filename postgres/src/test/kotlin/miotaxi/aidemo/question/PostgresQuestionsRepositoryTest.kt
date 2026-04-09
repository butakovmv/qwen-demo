package miotaxi.aidemo.question

import kotlinx.coroutines.test.runTest
import miotaxi.aidemo.BasePostgresTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class PostgresQuestionsRepositoryTest : BasePostgresTest() {
    private lateinit var questions: List<QuestionEntity>

    @BeforeEach
    fun setUpFixture() {
        cleanup()
        questions = questionGenerator.createAndInsert(2)
    }

    @Autowired
    private lateinit var questionsRepo: PostgresQuestionsRepository

    @Test
    fun `findAll returns questions from database`() =
        runTest {
            val stored = questionsRepo.findAll()

            assertEquals(questions.size, stored.size)
            questions.forEachIndexed { index, expected ->
                assertEquals(expected.id, stored[index].id)
                assertEquals(expected.text, stored[index].text)
            }
        }

    @Test
    fun `each question has non-blank id and text`() =
        runTest {
            val stored = questionsRepo.findAll()

            stored.forEach { q ->
                assertTrue(q.id.isNotBlank(), "id should not be blank")
                assertTrue(q.text.isNotBlank(), "text should not be blank")
            }
        }
}
