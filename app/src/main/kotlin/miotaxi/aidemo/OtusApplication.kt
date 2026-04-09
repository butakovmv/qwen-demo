package miotaxi.aidemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan
class OtusApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<OtusApplication>(*args)
}
