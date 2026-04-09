package miotaxi.aidemo.answer

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

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
