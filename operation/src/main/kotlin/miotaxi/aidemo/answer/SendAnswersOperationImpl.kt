package miotaxi.aidemo.answer

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
internal class SendAnswersOperationImpl(
    private val answersRepository: AnswersRepository,
) : SendAnswersOperation {
    private val log = LoggerFactory.getLogger(SendAnswersOperationImpl::class.java)

    override suspend fun execute(request: SendAnswersOperation.Request): SendAnswersOperation.Response {
        log.info("Saving {} answers", request.answers.size)
        val repoAnswers =
            request.answers.map { a ->
                AnswersRepository.Answer(a.id, a.text)
            }
        val success = answersRepository.saveAll(repoAnswers)
        log.info("Answers saved: success={}", success)
        return SendAnswersResponseImpl(success = success)
    }

    data class SendAnswersResponseImpl(
        override val success: Boolean,
    ) : SendAnswersOperation.Response
}
