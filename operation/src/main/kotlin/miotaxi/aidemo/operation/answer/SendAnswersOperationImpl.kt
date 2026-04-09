package miotaxi.aidemo.operation.answer

import org.springframework.stereotype.Component

@Component
internal class SendAnswersOperationImpl(
    private val answersRepository: AnswersRepository,
) : SendAnswersOperation {
    override suspend fun execute(request: SendAnswersOperation.Request): SendAnswersOperation.Response {
        val repoAnswers =
            request.answers.map { a ->
                AnswersRepository.Answer(a.id, a.text)
            }
        val success = answersRepository.saveAll(repoAnswers)
        return SendAnswersResponseImpl(success = success)
    }

    data class SendAnswersResponseImpl(
        override val success: Boolean,
    ) : SendAnswersOperation.Response
}
