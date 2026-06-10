package miotaxi.aidemo.question

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
internal class GetQuestionsOperationImpl(
    private val questionsRepository: QuestionsRepository,
) : GetQuestionsOperation {
    private val log = LoggerFactory.getLogger(GetQuestionsOperationImpl::class.java)

    override suspend fun execute(): GetQuestionsOperation.Response {
        log.info("Fetching all questions")
        val repoQuestions = questionsRepository.findAll()
        log.info("Found {} questions", repoQuestions.size)
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
