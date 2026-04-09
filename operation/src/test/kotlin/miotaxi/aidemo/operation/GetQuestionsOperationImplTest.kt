package miotaxi.aidemo.operation

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetQuestionsOperationImplTest {
    private val repository = InMemoryQuestionsRepository()
    private val operation = GetQuestionsOperationImpl(repository)

    @Test
    fun `execute returns list of questions`() =
        runTest {
            val result = operation.execute()

            assertTrue(result.questions.isNotEmpty())
            assertEquals(2, result.questions.size)
        }

    @Test
    fun `each question has non-blank id and text`() =
        runTest {
            val result = operation.execute()

            for (q in result.questions) {
                assertTrue(q.id.isNotBlank())
                assertTrue(q.text.isNotBlank())
            }
        }
}
