package miotaxi.aidemo.question

import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Репозиторий вопросов на основе Spring Data R2DBC.
 * Активируется с профилями `local`, `docker`, `pg` или `h2`.
 * Делегирует {@link QuestionsCrudRepository}.
 * Реализует интерфейс {@link QuestionsRepository} из модуля operation.
 */
@Component
@Profile("pg", "h2")
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
