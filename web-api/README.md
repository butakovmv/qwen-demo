# Модуль web-api

## Назначение
REST API-слой: контроллеры, конфигурация Spring, документация OpenAPI/Swagger. Принимает HTTP-запросы, делегирует выполнение интерфейсам Use Case (модуль `operation`), сериализует ответы в JSON. Отвечает за версионирование API (`/api/v1/...`).

## Содержание
Kotlin-классы: REST-контроллеры (`@RestController`), конфигурации Spring (`@Configuration`), OpenAPI-конфигурация.

## Языки и фреймворки

| Категория | Технология | Версия |
|---|---|---|
| Язык | Kotlin | 2.1.10 |
| JVM-плагин | kotlin("jvm") | 2.1.10 |
| Spring-плагин | kotlin("plugin.spring") | 2.1.10 |
| Dependency Management | io.spring.dependency-management | 1.1.7 |
| Spring Boot Starter WebFlux | org.springframework.boot | 3.4.4 (BOM) |
| Jackson Kotlin Module | com.fasterxml.jackson.module | из BOM |
| Kotlinx Coroutines Reactor | org.jetbrains.kotlinx | из BOM |
| SpringDoc OpenAPI (Swagger UI) | org.springdoc | 2.7.0 |
| ktlint | org.jlleitschuh.gradle.ktlint | 12.3.0 |

## Зависимости
- **Зависит от:** `:operation` (интерфейсы Use Case)
- **Зависит от (runtime):** `app` — собирает и запускает модуль
- **Зависимости (тесты):** `spring-boot-starter-test`, `reactor-test`, `mockk:1.13.13`

## Связи
Зависит от модуля `operation` — вызывает интерфейсы Use Case. Через HTTP-контракт обслуживает модуль `front`. Сам модуль `web-api` не является самодостаточным — подключается как зависимость в `app`.

## Инструменты
- `@RestController` + `suspend fun` — реактивные endpoint-ы на WebFlux
- `@WebFluxTest` — тестирование контроллеров с `WebTestClient`
- Swagger UI — документация API (http://localhost:8080/swagger-ui.html)
- ktlint — проверка стиля кода

## Сборка
Не запускается самостоятельно. Собирается в JAR-библиотеку, которая подключается в модуль `app`. Запуск через `./gradlew :app:bootRun` или сборка `./gradlew :app:bootJar`.

## Тесты
`@WebFluxTest` на моках Use Case. Тесты контролируют коды ответов, валидацию запросов и корректность JSON-ответов.