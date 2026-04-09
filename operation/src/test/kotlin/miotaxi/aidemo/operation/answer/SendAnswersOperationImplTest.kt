package miotaxi.aidemo.operation.answer

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SendAnswersOperationImplTest {
    private val repository = InMemoryAnswersRepository()
    private val operation = SendAnswersOperationImpl(repository)

    @Test
    fun `execute returns success`() =
        runTest {
            val request =
                object : SendAnswersOperation.Request {
                    override val answers =
                        listOf(
                            SendAnswersOperation.Answer("550e8400-e29b-41d4-a716-446655440000", "My name is John"),
                        )
                }

            val result = operation.execute(request)

            assertTrue(result.success)
        }

    @Test
    fun `execute stores answers in repository`() =
        runTest {
            val request =
                object : SendAnswersOperation.Request {
                    override val answers =
                        listOf(
                            SendAnswersOperation.Answer("q1", "Answer 1"),
                            SendAnswersOperation.Answer("q2", "Answer 2"),
                        )
                }

            operation.execute(request)
            val saved = repository.findAll()

            assertEquals(2, saved.size)
            assertEquals("q1", saved[0].questionId)
            assertEquals("Answer 1", saved[0].text)
        }
}
