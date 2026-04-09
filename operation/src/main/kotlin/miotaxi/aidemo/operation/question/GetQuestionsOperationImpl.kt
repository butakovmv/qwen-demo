package miotaxi.aidemo.operation.question

import org.springframework.stereotype.Component

@Component
internal class GetQuestionsOperationImpl(
    private val questionsRepository: QuestionsRepository,
) : GetQuestionsOperation {
    override suspend fun execute(): GetQuestionsOperation.Response {
        val repoQuestions = questionsRepository.findAll()
        return GetQuestionsResponseImpl(
            questions =
                repoQuestions.map { q ->
                    GetQuestionsOperation.Question(q.id, q.text)
                },
        )
    }

    data class GetQuestionsResponseImpl(
        override val questions: List<GetQuestionsOperation.Question>,
    ) : GetQuestionsOperation.Response
}
