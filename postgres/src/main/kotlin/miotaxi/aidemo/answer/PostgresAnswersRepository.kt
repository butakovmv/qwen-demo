package miotaxi.aidemo.answer

import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Репозиторий ответов на основе Spring Data R2DBC.
 * Активируется с профилями `local`, `docker`, `pg` или `h2`.
 * Делегирует {@link AnswersCrudRepository}.
 * Реализует интерфейс {@link AnswersRepository} из модуля operation.
 */
@Component
@Profile("pg", "h2")
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
