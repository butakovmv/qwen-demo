package miotaxi.aidemo

import miotaxi.aidemo.answer.AnswerGenerator
import miotaxi.aidemo.question.QuestionGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles

/**
 * Базовый класс для интеграционных тестов postgres-модуля.
 *
 * Предоставляет готовые генераторы с подключённым dbClient.
 * Перед каждым тестом очищает все таблицы.
 */
@SpringBootTest(classes = [PostgresTestConfig::class])
@ActiveProfiles("h2")
@ComponentScan
@EnableR2dbcRepositories
internal abstract class BasePostgresTest {
    @Autowired
    protected lateinit var dbClient: DatabaseClient

    protected val questionGenerator: QuestionGenerator
        get() = QuestionGenerator(dbClient)

    protected val answerGenerator: AnswerGenerator
        get() = AnswerGenerator(dbClient)

    protected fun cleanup() {
        ClassPathResource("db/cleanup-h2.sql").inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("--")) {
                    dbClient.sql(trimmed).then().block()
                }
            }
        }
    }
}
