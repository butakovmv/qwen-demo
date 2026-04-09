package miotaxi.aidemo.question

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * R2DBC репозиторий для вопросов.
 * Spring Data автоматически создаст реализацию.
 */
internal interface QuestionsCrudRepository : CoroutineCrudRepository<QuestionEntity, String>
