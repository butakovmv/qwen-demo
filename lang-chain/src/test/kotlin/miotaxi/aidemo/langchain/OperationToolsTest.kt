package miotaxi.aidemo.langchain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import miotaxi.aidemo.answer.SendAnswersOperation
import miotaxi.aidemo.question.GetQuestionsOperation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OperationToolsTest {
    private val sendAnswersOperation = mockk<SendAnswersOperation>()
    private val getQuestionsOperation = mockk<GetQuestionsOperation>()
    private val tools = OperationTools(sendAnswersOperation, getQuestionsOperation)

    @Test
    fun `getQuestions returns formatted list of questions`() =
        runTest {
            coEvery { getQuestionsOperation.execute() } returns
                object : GetQuestionsOperation.Response {
                    override val questions =
                        listOf(
                            GetQuestionsOperation.Question("q1", "What is your name?"),
                            GetQuestionsOperation.Question("q2", "What is your quest?"),
                        )
                }

            val result = tools.getQuestions()

            assertTrue(result.contains("q1"))
            assertTrue(result.contains("What is your name?"))
            assertTrue(result.contains("q2"))
            assertTrue(result.contains("What is your quest?"))
            assertTrue(result.contains("Найдено вопросов: 2"))
        }

    @Test
    fun `getQuestions returns empty list message when no questions`() =
        runTest {
            coEvery { getQuestionsOperation.execute() } returns
                object : GetQuestionsOperation.Response {
                    override val questions = emptyList<GetQuestionsOperation.Question>()
                }

            val result = tools.getQuestions()

            assertTrue(result.contains("Найдено вопросов: 0"))
        }

    @Test
    fun `sendAnswers parses JSON and calls operation with answers`() =
        runTest {
            val captured = mutableListOf<List<SendAnswersOperation.Answer>>()
            coEvery { sendAnswersOperation.execute(any()) } coAnswers {
                captured += firstArg<SendAnswersOperation.Request>().answers
                object : SendAnswersOperation.Response {
                    override val success = true
                }
            }

            val json = """[{"id":"q1","text":"My name is John"}]"""
            val result = tools.sendAnswers(json)

            assertTrue(result.contains("успешно"))
            assertEquals(1, captured.size)
            assertEquals(1, captured[0].size)
            assertEquals("q1", captured[0][0].id)
            assertEquals("My name is John", captured[0][0].text)
        }

    @Test
    fun `sendAnswers parses multiple answers from JSON`() =
        runTest {
            val captured = mutableListOf<List<SendAnswersOperation.Answer>>()
            coEvery { sendAnswersOperation.execute(any()) } coAnswers {
                captured += firstArg<SendAnswersOperation.Request>().answers
                object : SendAnswersOperation.Response {
                    override val success = true
                }
            }

            val json = """[{"id":"q1","text":"Answer 1"},{"id":"q2","text":"Answer 2"}]"""
            val result = tools.sendAnswers(json)

            assertTrue(result.contains("2"))
            assertEquals(2, captured[0].size)
            assertEquals("Answer 1", captured[0][0].text)
            assertEquals("Answer 2", captured[0][1].text)
        }

    @Test
    fun `sendAnswers returns failure message when operation reports failure`() =
        runTest {
            coEvery { sendAnswersOperation.execute(any()) } returns
                object : SendAnswersOperation.Response {
                    override val success = false
                }

            val result = tools.sendAnswers("""[{"id":"q1","text":"x"}]""")

            assertTrue(result.contains("Не удалось"))
        }

    @Test
    fun `sendAnswers throws on invalid JSON`() {
        assertThrows(IllegalArgumentException::class.java) {
            tools.sendAnswers("not a json")
        }
    }
}
