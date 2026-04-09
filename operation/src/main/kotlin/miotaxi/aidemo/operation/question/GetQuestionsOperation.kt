package miotaxi.aidemo.operation.question

/**
 * Операция получения списка вопросов.
 *
 * Возвращает все доступные вопросы для отображения пользователю.
 */
interface GetQuestionsOperation {
    suspend fun execute(): Response

    /** Ответ операции получения вопросов. */
    interface Response {
        /** Список вопросов. */
        val questions: List<Question>
    }

    /** Вопрос с уникальным идентификатором и текстом. */
    data class Question(
        /** Уникальный идентификатор вопроса (UUID). */
        val id: String,
        /** Текст вопроса. */
        val text: String,
    )
}
