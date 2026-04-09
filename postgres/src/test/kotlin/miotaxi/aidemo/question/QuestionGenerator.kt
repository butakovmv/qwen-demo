package miotaxi.aidemo.question

import org.springframework.r2dbc.core.DatabaseClient
import java.util.UUID

/**
 * Генератор тестовых данных для вопросов.
 * Генерирует случайные сущности и сразу сохраняет их в БД.
 */
internal class QuestionGenerator(
    private val dbClient: DatabaseClient,
) {
    fun randomId(): String = UUID.randomUUID().toString()

    fun randomText(): String = "Question ${UUID.randomUUID().toString().substring(0, 8)}"

    fun randomQuestion(): QuestionEntity = QuestionEntity(randomId(), randomText())

    fun createAndInsert(count: Int = 2): List<QuestionEntity> =
        List(count) { randomQuestion() }
            .takeIf { it.isNotEmpty() }
            .orEmpty()
            .also { questions ->
                val sql =
                    "INSERT INTO questions (id, text) VALUES " +
                        questions.joinToString { q -> "('${q.id}', '${q.text}')" }
                dbClient.sql(sql).then().block()
            }
}
