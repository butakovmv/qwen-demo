package miotaxi.aidemo.operation

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
internal class HelloWorldOperationImpl : HelloWorldOperation {
    private companion object {
        const val DELAY_MS = 100L
    }

    override suspend fun execute(): HelloWorldOperation.Response {
        delay(DELAY_MS) // имитация асинхронной работы
        return HelloResponseImpl(message = "Hello, World!")
    }

    data class HelloResponseImpl(override val message: String) : HelloWorldOperation.Response
}
