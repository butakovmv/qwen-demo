package miotaxi.aidemo.webapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Минимальная конфигурация для @WebFluxTest.
 * @SpringBootApplication нужен только для того, чтобы Spring Boot Test нашёл точку входа.
 * Контроллеры регистрируются явно через @Import(TestConfig::class), поэтому сканирование отключено.
 */
@SpringBootApplication
@ComponentScan
internal class TestApplication
