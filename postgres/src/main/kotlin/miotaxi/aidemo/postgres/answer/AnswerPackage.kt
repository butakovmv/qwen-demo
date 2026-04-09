package miotaxi.aidemo.postgres.answer

import kotlinx.coroutines.flow.toList
import miotaxi.aidemo.operation.answer.AnswersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Component

/**
 * Сущность ответа для R2DBC.
 */
@Table("answers")
internal data class AnswerEntity(
    @Id
    val id: Long? = null,
    @Column("question_id")
    val questionId: String,
    val text: String,
)

/**
 * R2DBC репозиторий для ответов.
 * Spring Data автоматически создаст реализацию.
 */
internal interface AnswersCrudRepository : CoroutineCrudRepository<AnswerEntity, Long>

/**
 * Репозиторий ответов на основе Spring Data R2DBC.
 * Активируется только с профилями `local` или `docker`.
 * Делегирует {@link AnswersCrudRepository}.
 * Реализует интерфейс {@link AnswersRepository} из модуля operation.
 */
@Component
@Profile("local", "docker", "pg", "h2")
internal class PostgresAnswersRepository
    @Autowired
    internal constructor(
        private val answersCrudRepository: AnswersCrudRepository,
    ) : AnswersRepository {
        override suspend fun saveAll(answers: List<AnswersRepository.Answer>): Boolean {
            if (answers.isEmpty()) return true

            answersCrudRepository.saveAll(
                answers.map { answer ->
                    AnswerEntity(
                        questionId = answer.questionId,
                        text = answer.text,
                    )
                },
            ).toList()
            return true
        }

        override suspend fun findAll(): List<AnswersRepository.Answer> {
            return answersCrudRepository.findAll()
                .toList()
                .map { entity ->
                    AnswersRepository.Answer(
                        questionId = entity.questionId,
                        text = entity.text,
                    )
                }
        }
    }
