package miotaxi.aidemo.langchain

import dev.langchain4j.agent.tool.Tool
import kotlinx.coroutines.runBlocking
import miotaxi.aidemo.answer.SendAnswersOperation
import miotaxi.aidemo.question.GetQuestionsOperation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Инструменты (tools), которые LLM может вызвать для выполнения операций доменов
 * `answer` и `question`.
 *
 * Каждый метод аннотирован {@link Tool} с описанием на естественном языке — это
 * описание видит LLM при выборе инструмента.
 */
@Component
internal class OperationTools(
    private val sendAnswersOperation: SendAnswersOperation,
    private val getQuestionsOperation: GetQuestionsOperation,
) {
    private val log = LoggerFactory.getLogger(OperationTools::class.java)

    /**
     * Получить список доступных вопросов.
     */
    @Tool(
        "Получить список всех доступных вопросов с их идентификаторами и текстами. " +
            "Используй, когда пользователь хочет увидеть, какие вопросы есть в системе " +
            "или какие вопросы нужно задать.",
    )
    fun getQuestions(): String =
        runBlocking {
            log.debug("getQuestions called")
            val response = getQuestionsOperation.execute()
            log.debug("getQuestions found {}", response.questions)
            buildString {
                append("Найдено вопросов: ${response.questions.size}\n")
                response.questions.forEachIndexed { idx, q ->
                    append("${idx + 1}. [${q.id}] ${q.text}\n")
                }
            }.trimEnd()
        }

    /**
     * Отправить ответы пользователя на вопросы.
     */
    @Tool(
        "Сохранить ответы пользователя на ранее полученные вопросы. " +
            "answersJson — JSON-массив объектов вида {\"id\":\"<uuid вопроса>\",\"text\":\"<текст ответа>\"}. " +
            "Возвращает текстовое описание результата сохранения.",
    )
    fun sendAnswers(answersJson: String): String =
        runBlocking {
            log.debug("sendAnswers called with: {}", answersJson)
            val answers = parseAnswers(answersJson)
            log.debug("sendAnswers parsed {} answers", answers.size)
            val response =
                sendAnswersOperation.execute(
                    object : SendAnswersOperation.Request {
                        override val answers = answers
                    },
                )
            if (response.success) {
                "Ответы успешно сохранены (${answers.size} шт.)"
            } else {
                "Не удалось сохранить ответы"
            }
        }

    private fun parseAnswers(json: String): List<SendAnswersOperation.Answer> {
        val pairs = ANSWER_REGEX.findAll(json).toList()
        log.debug("parseAnswers: found {} matches in {}", pairs.size, json.take(LOG_TEXT_MAX_LENGTH))
        require(pairs.isNotEmpty()) { "Не удалось распарсить ответы из: $json" }
        return pairs.map { match ->
            val (id, text) = match.destructured
            SendAnswersOperation.Answer(id = id, text = text)
        }
    }

    private companion object {
        const val LOG_TEXT_MAX_LENGTH = 200
        val ANSWER_REGEX = Regex("""\{\s*"id"\s*:\s*"([^"]+)"\s*,\s*"text"\s*:\s*"([^"]*)"\s*}""")
    }
}
