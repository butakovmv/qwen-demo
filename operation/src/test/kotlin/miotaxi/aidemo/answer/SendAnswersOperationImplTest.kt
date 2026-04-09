package miotaxi.aidemo.answer

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SendAnswersOperationImplTest {
    private val repository = mockk<AnswersRepository>(relaxed = true)
    private val operation = SendAnswersOperationImpl(repository)

    @Test
    fun `execute returns success`() =
        runTest {
            coEvery { repository.saveAll(any()) } returns true

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
            val capturedAnswers = mutableListOf<List<AnswersRepository.Answer>>()
            coEvery { repository.saveAll(capture(capturedAnswers)) } returns true

            val request =
                object : SendAnswersOperation.Request {
                    override val answers =
                        listOf(
                            SendAnswersOperation.Answer("q1", "Answer 1"),
                            SendAnswersOperation.Answer("q2", "Answer 2"),
                        )
                }

            operation.execute(request)

            assertEquals(1, capturedAnswers.size)
            assertEquals(2, capturedAnswers[0].size)
            assertEquals("q1", capturedAnswers[0][0].questionId)
            assertEquals("Answer 1", capturedAnswers[0][0].text)
            coVerify { repository.saveAll(any()) }
        }
}
