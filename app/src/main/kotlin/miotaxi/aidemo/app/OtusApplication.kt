package miotaxi.aidemo.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["miotaxi.aidemo.app", "miotaxi.aidemo.operation", "miotaxi.aidemo.webapi"])
class OtusApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<OtusApplication>(*args)
}
