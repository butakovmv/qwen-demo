package miotaxi.aidemo.naturalLanguage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import miotaxi.aidemo.answer.SendAnswersOperation
import miotaxi.aidemo.langchain.NaturalLanguageCommandService
import miotaxi.aidemo.question.GetQuestionsOperation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Реализация операции отправки команды на естественном языке через langchain4j.
 *
 * Сначала через LLM определяется намерение пользователя (GET_QUESTIONS / SEND_ANSWERS),
 * затем соответствующая операция выполняется напрямую, без участия LLM.
 */
@Component
internal class SendNaturalLanguageCommandOperationImpl(
    private val naturalLanguageCommandService: NaturalLanguageCommandService,
    private val getQuestionsOperation: GetQuestionsOperation,
    private val sendAnswersOperation: SendAnswersOperation,
) : SendNaturalLanguageCommandOperation {
    private val log = LoggerFactory.getLogger(SendNaturalLanguageCommandOperationImpl::class.java)

    companion object {
        private const val LOG_TEXT_MAX_LENGTH = 200
        private const val GET_QUESTIONS = "GET_QUESTIONS"
        private const val SEND_ANSWERS_PREFIX = "SEND_ANSWERS:"
        private const val ERROR_PREFIX = "ERROR:"
    }

    override suspend fun execute(
        request: SendNaturalLanguageCommandOperation.Request,
    ): SendNaturalLanguageCommandOperation.Response {
        log.info("Interpreting command: {}", request.text.take(LOG_TEXT_MAX_LENGTH))
        val result =
            runCatching { withContext(Dispatchers.IO) { naturalLanguageCommandService.interpret(request.text) } }
        val exception = result.exceptionOrNull()
        if (exception != null) {
            log.warn("Failed to interpret command: {}", exception.message ?: exception::class.simpleName)
            return errorResponse("Не удалось обработать команду: ${exception.message ?: exception::class.simpleName}")
        }
        val raw = result.getOrNull()
        log.info("Command interpreted: {} chars received", raw?.length ?: 0)
        return if (raw == null) {
            log.warn("LLM returned null response")
            errorResponse("Не удалось обработать команду: LLM вернул пустой ответ")
        } else {
            executeIntent(raw.trim())
        }
    }

    private suspend fun executeIntent(response: String): SendNaturalLanguageCommandOperation.Response {
        val intent = response.trim()
        return when {
            intent == GET_QUESTIONS -> handleGetQuestions()
            intent.startsWith(SEND_ANSWERS_PREFIX) -> handleSendAnswers(intent.removePrefix(SEND_ANSWERS_PREFIX).trim())
            intent.startsWith(ERROR_PREFIX) -> {
                val msg = intent.removePrefix(ERROR_PREFIX).trim()
                log.warn("LLM reported error: {}", msg)
                errorResponse(msg)
            }
            else -> {
                log.info("Unknown intent from LLM, treating as error: {}", intent)
                errorResponse("Не удалось распознать команду")
            }
        }
    }

    private suspend fun handleGetQuestions(): SendNaturalLanguageCommandOperation.Response {
        log.info("Executing GET_QUESTIONS")
        val questions = getQuestionsOperation.execute()
        val text =
            buildString {
                append("Найдено вопросов: ${questions.questions.size}\n")
                questions.questions.forEachIndexed { idx, q ->
                    append("${idx + 1}. [${q.id}] ${q.text}\n")
                }
            }.trimEnd()
        log.info("Found {} questions", questions.questions.size)
        return successResponse(text)
    }

    private suspend fun handleSendAnswers(json: String): SendNaturalLanguageCommandOperation.Response {
        log.info("Executing SEND_ANSWERS with: {}", json.take(LOG_TEXT_MAX_LENGTH))
        val answers = parseAnswersJson(json)
        if (answers.isEmpty()) {
            log.warn("No answers parsed from: {}", json)
            return errorResponse("Не удалось распарсить ответы")
        }
        val result =
            sendAnswersOperation.execute(
                object : SendAnswersOperation.Request {
                    override val answers = answers
                },
            )
        return if (result.success) {
            log.info("Answers saved successfully: {} items", answers.size)
            successResponse("Ответы успешно сохранены (${answers.size} шт.)")
        } else {
            log.warn("Failed to save answers")
            errorResponse("Не удалось сохранить ответы")
        }
    }

    private fun parseAnswersJson(json: String): List<SendAnswersOperation.Answer> {
        val regex = Regex("""\{\s*"id"\s*:\s*"([^"]+)"\s*,\s*"text"\s*:\s*"([^"]*)"\s*}""")
        return regex.findAll(json).map { match ->
            val (id, text) = match.destructured
            SendAnswersOperation.Answer(id = id, text = text)
        }.toList()
    }

    private fun successResponse(text: String): SendNaturalLanguageCommandOperation.Response =
        SendNaturalLanguageCommandResponseImpl(success = true, text = text)

    private fun errorResponse(text: String): SendNaturalLanguageCommandOperation.Response =
        SendNaturalLanguageCommandResponseImpl(success = false, text = text)

    private data class SendNaturalLanguageCommandResponseImpl(
        override val success: Boolean,
        override val text: String,
    ) : SendNaturalLanguageCommandOperation.Response
}
