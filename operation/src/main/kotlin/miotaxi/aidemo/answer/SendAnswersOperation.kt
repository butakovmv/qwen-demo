package miotaxi.aidemo.answer

/**
 * Операция отправки ответов на вопросы.
 *
 * Принимает список ответов от пользователя и возвращает результат их обработки.
 */
interface SendAnswersOperation {
    suspend fun execute(request: Request): Response

    /** Ответ пользователя на вопрос. */
    data class Answer(
        /** Уникальный идентификатор вопроса (UUID). */
        val id: String,
        /** Текст ответа пользователя. */
        val text: String,
    )

    /** Запрос на отправку ответов. */
    interface Request {
        /** Список ответов пользователя. */
        val answers: List<Answer>
    }

    /** Результат операции отправки ответов. */
    interface Response {
        /** Признак успешной обработки ответов. */
        val success: Boolean
    }
}
