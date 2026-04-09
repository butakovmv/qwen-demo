package miotaxi.aidemo.operation.answer

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Хранилище ответов в оперативной памяти.
 * Активно по умолчанию (когда НЕ используются профили local/docker).
 */
@Component
@Profile("!local & !docker")
internal class InMemoryAnswersRepository : AnswersRepository {
    private val storedAnswers = mutableListOf<AnswersRepository.Answer>()

    override suspend fun saveAll(answers: List<AnswersRepository.Answer>): Boolean {
        storedAnswers.addAll(answers)
        return true
    }

    override suspend fun findAll(): List<AnswersRepository.Answer> = storedAnswers.toList()
}
