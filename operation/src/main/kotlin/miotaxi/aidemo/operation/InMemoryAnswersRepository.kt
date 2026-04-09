package miotaxi.aidemo.operation

import org.springframework.stereotype.Component

/**
 * Хранилище ответов в оперативной памяти.
 */
@Component
internal class InMemoryAnswersRepository : AnswersRepository {
    private val storedAnswers = mutableListOf<AnswersRepository.Answer>()

    override suspend fun saveAll(answers: List<AnswersRepository.Answer>): Boolean {
        storedAnswers.addAll(answers)
        return true
    }

    override suspend fun findAll(): List<AnswersRepository.Answer> = storedAnswers.toList()
}
