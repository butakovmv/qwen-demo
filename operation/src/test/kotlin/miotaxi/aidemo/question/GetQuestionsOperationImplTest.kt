package miotaxi.aidemo.question

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetQuestionsOperationImplTest {
    private val repository = mockk<QuestionsRepository>()
    private val operation = GetQuestionsOperationImpl(repository)

    @Test
    fun `execute returns list of questions`() =
        runTest {
            coEvery { repository.findAll() } returns
                listOf(
                    QuestionsRepository.Question("550e8400-e29b-41d4-a716-446655440000", "What is your name?"),
                    QuestionsRepository.Question("6ba7b810-9dad-11d1-80b4-00c04fd430c8", "What is your quest?"),
                )

            val result = operation.execute()

            assertTrue(result.questions.isNotEmpty())
            assertEquals(2, result.questions.size)
        }

    @Test
    fun `each question has non-blank id and text`() =
        runTest {
            coEvery { repository.findAll() } returns
                listOf(
                    QuestionsRepository.Question("q1", "Question 1"),
                    QuestionsRepository.Question("q2", "Question 2"),
                )

            val result = operation.execute()

            for (q in result.questions) {
                assertTrue(q.id.isNotBlank())
                assertTrue(q.text.isNotBlank())
            }
        }
}
