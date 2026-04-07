package miotaxi.aidemo.webapi.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import miotaxi.aidemo.operation.HelloWorldOperation

@RestController
@RequestMapping("/api/v1")
internal class HelloWorldController(
    private val helloWorldOperation: HelloWorldOperation,
) {
    @GetMapping("/hello-world")
    suspend fun helloWorld(): HelloWorldOperation.Response {
        return helloWorldOperation.execute()
    }
}
