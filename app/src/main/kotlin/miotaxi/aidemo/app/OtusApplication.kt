package miotaxi.aidemo.app

import miotaxi.aidemo.operation.OperationMarker
import miotaxi.aidemo.postgres.PostgresMarker
import miotaxi.aidemo.webapi.WebapiMarker
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(
    basePackageClasses = [
        AppMarker::class,
        OperationMarker::class,
        PostgresMarker::class,
        WebapiMarker::class,
    ],
)
class OtusApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<OtusApplication>(*args)
}
