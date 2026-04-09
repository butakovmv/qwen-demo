package miotaxi.aidemo.answer

/**
 * Репозиторий ответов.
 */
interface AnswersRepository {
    /** Сохранить список ответов. Возвращает признак успешной операции. */
    suspend fun saveAll(answers: List<Answer>): Boolean

    /** Получить все сохранённые ответы. */
    suspend fun findAll(): List<Answer>

    /** Ответ пользователя на вопрос. */
    data class Answer(
        val questionId: String,
        val text: String,
    )
}
