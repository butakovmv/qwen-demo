package miotaxi.aidemo.answer

import miotaxi.aidemo.question.QuestionEntity
import org.springframework.r2dbc.core.DatabaseClient
import java.util.UUID

/**
 * Генератор тестовых данных для ответов.
 * Генерирует случайные сущности и сразу сохраняет их в БД.
 */
internal class AnswerGenerator(
    private val dbClient: DatabaseClient,
) {
    fun randomText(): String = "Answer ${UUID.randomUUID().toString().substring(0, 8)}"

    fun createAndInsertFor(questions: List<QuestionEntity>): List<AnswerEntity> =
        questions.map { AnswerEntity(questionId = it.id, text = randomText()) }
            .takeIf { it.isNotEmpty() }
            .orEmpty()
            .also { answers ->
                val sql =
                    "INSERT INTO answers (question_id, text) VALUES " +
                        answers.joinToString { a -> "('${a.questionId}', '${a.text}')" }
                dbClient.sql(sql).then().block()
            }
}
