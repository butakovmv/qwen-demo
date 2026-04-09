package miotaxi.aidemo.operation.question

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Хранилище вопросов в оперативной памяти.
 * Инициализируется набором тестовых вопросов.
 * Активен по умолчанию (когда НЕ используются профили local/docker).
 */
@Component
@Profile("!local & !docker")
internal class InMemoryQuestionsRepository : QuestionsRepository {
    private val questions: List<QuestionsRepository.Question> =
        listOf(
            QuestionsRepository.Question("550e8400-e29b-41d4-a716-446655440000", "What is your name?"),
            QuestionsRepository.Question("6ba7b810-9dad-11d1-80b4-00c04fd430c8", "What is your quest?"),
        )

    override suspend fun findAll(): List<QuestionsRepository.Question> = questions.toList()
}
