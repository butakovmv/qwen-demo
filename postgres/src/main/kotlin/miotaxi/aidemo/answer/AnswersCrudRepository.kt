package miotaxi.aidemo.answer

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * R2DBC репозиторий для ответов.
 * Spring Data автоматически создаст реализацию.
 */
internal interface AnswersCrudRepository : CoroutineCrudRepository<AnswerEntity, Long>
