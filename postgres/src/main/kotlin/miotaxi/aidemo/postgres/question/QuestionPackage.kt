package miotaxi.aidemo.postgres.question

import kotlinx.coroutines.flow.toList
import miotaxi.aidemo.operation.question.QuestionsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Component

/**
 * Сущность вопроса для R2DBC.
 */
@Table("questions")
internal data class QuestionEntity(
    @Id
    val id: String,
    val text: String,
)

/**
 * R2DBC репозиторий для вопросов.
 * Spring Data автоматически создаст реализацию.
 */
internal interface QuestionsCrudRepository : CoroutineCrudRepository<QuestionEntity, String>

/**
 * Репозиторий вопросов на основе Spring Data R2DBC.
 * Активируется только с профилями `local` или `docker`.
 * Делегирует {@link QuestionsCrudRepository}.
 * Реализует интерфейс {@link QuestionsRepository} из модуля operation.
 */
@Component
@Profile("local", "docker", "pg", "h2")
internal class PostgresQuestionsRepository
    @Autowired
    internal constructor(
        private val questionsCrudRepository: QuestionsCrudRepository,
    ) : QuestionsRepository {
        override suspend fun findAll(): List<QuestionsRepository.Question> {
            return questionsCrudRepository.findAll()
                .toList()
                .map { entity ->
                    QuestionsRepository.Question(
                        id = entity.id,
                        text = entity.text,
                    )
                }
        }
    }
