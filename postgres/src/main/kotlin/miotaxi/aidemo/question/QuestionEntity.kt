package miotaxi.aidemo.question

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Сущность вопроса для R2DBC.
 */
@Table("questions")
internal data class QuestionEntity(
    @Id
    val id: String,
    val text: String,
)
