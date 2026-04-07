package miotaxi.aidemo.operation

interface HelloWorldOperation {
    suspend fun execute(): Response

    interface Response {
        val message: String
    }
}
