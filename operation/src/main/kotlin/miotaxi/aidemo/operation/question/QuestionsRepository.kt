package miotaxi.aidemo.operation.question

/**
 * Репозиторий вопросов.
 */
interface QuestionsRepository {
    /** Получить все вопросы. */
    suspend fun findAll(): List<Question>

    /** Вопрос с уникальным идентификатором и текстом. */
    data class Question(
        val id: String,
        val text: String,
    )
}
