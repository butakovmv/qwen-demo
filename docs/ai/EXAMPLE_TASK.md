# Примеры выполнения типовых задач

## 1. Добавление нового модуля-адаптера

Модуль-адаптер реализует порты (интерфейсы) из `operation` для конкретной технологии (БД, AI, Kafka, Feign и т.д.).

### Последовательность шагов

1. **Создать структуру каталогов модуля**

   ```
   <module-name>/
   ├── build.gradle.kts
   └── src/
       ├── main/
       │   ├── kotlin/miotaxi/aidemo/<domain>/
       │   └── resources/
       └── test/
           └── kotlin/miotaxi/aidemo/<domain>/
   ```

2. **Создать `build.gradle.kts`** — подключить плагины и зависимости:

   ```kotlin
   plugins {
       kotlin("jvm")
       kotlin("plugin.spring")
       id("io.spring.dependency-management")
       jacoco
   }

   jacoco { toolVersion = "0.8.12" }
   tasks.test { finalizedBy(tasks.jacocoTestReport) }

   dependencyManagement {
       imports { mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1") }
   }

   dependencies {
       implementation(project(":operation"))
       // + зависимости под технологию: spring-boot-starter-webflux, spring-boot-starter-data-r2dbc, langchain4j и т.д.
       testImplementation("org.springframework.boot:spring-boot-starter-test")
       testImplementation("io.mockk:mockk:1.13.13")
       // + при необходимости coroutines-test, reactor-test
   }

   tasks.withType<Test> { useJUnitPlatform() }
   ```

   Правила для зависимостей:
   - `implementation(project(":operation"))` обязателен — это единственный канал связи с доменом
   - Если модуль использует Spring Boot autoconfig (starter'ы), нужен `spring-boot-dependencies` в `dependencyManagement`
   - Если модуль использует только `@Component`/`@Configuration` (как `operation`), достаточно `spring-context`
   - `jackson-module-kotlin` — если будет JSON-парсинг
   - `kotlinx-coroutines-core` — если без Spring Boot (в модулях без `dependencyManagement`)

3. **Зарегистрировать модуль в `settings.gradle.kts`**

   ```kotlin
   include("<module-name>")
   ```

   Строки располагаются в алфавитном порядке после `include("lang-chain")`.

4. **Подключить модуль в `app/build.gradle.kts`**

   ```kotlin
   implementation(project(":<module-name>"))
   ```

   Строка добавляется в блок `dependencies` в алфавитном порядке.

5. **Добавить конфигурацию в `app/src/main/resources/application.yml`** (если модуль требует настройки)

   ```yaml
   <module-config-prefix>:
     <property>: ${ENV_VAR:default}
   ```

6. **Добавить env-vars в `docker-compose.yaml`** (если применимо)

   ```yaml
   backend:
     environment:
       - PREFIX_PROPERTY=${PREFIX_PROPERTY:-default}
   ```

7. **Написать реализацию интерфейсов из `operation`**

   - Класс помечается `internal class` и `@Component`
   - Пакет: `miotaxi.aidemo.<domain>` (или `miotaxi.aidemo.<module-name>.<domain>` если логика не относится к домену)
   - Реализует публичный интерфейс из `operation`
   - Если реализация живёт не в `operation`, а в адаптере — `operation` содержит только интерфейс (без `Impl`)

   **Пример** (`postgres/PostgresQuestionsRepository.kt`):
   - `@Component @Profile("pg","h2")`
   - Реализует `QuestionsRepository` из `operation`
   - Папка `miotaxi.aidemo.question`

8. **Написать Configuration class** (если нужны сложные бины или `@ConditionalOnMissingBean`)

   - `@Configuration internal class`
   - Используется `@ConditionalOnMissingBean` для возможности подмены в тестах

9. **Обновить `ApplicationContextTest.kt`**

   - Добавить `@Primary @Bean`, возвращающий `mockk()` для новых интерфейсов, которые требует контекст
   - Если в модуле есть репозитории c `@Profile`, добавить их в `excludeFilters`
   - Если модуль требует property для конфигурации, добавить в `@TestPropertySource`

10. **Написать тесты модуля**

    - Unit-тесты для логики на mockk
    - Интеграционные тесты для слоя данных (если применимо)

11. **Проверить прохождение линтеров**

    ```bash
    ./gradlew :<module-name>:detekt :<module-name>:ktlintCheck
    ```

---

## 2. Добавление новой операции

Операция объявляется как интерфейс в модуле `operation` и при необходимости реализуется в нём же или в адаптере.

### Последовательность шагов

1. **Создать интерфейс операции** в `operation/src/main/kotlin/miotaxi/aidemo/<domain>/<OperationName>Operation.kt`

   ```kotlin
   package miotaxi.aidemo.<domain>

   interface <OperationName>Operation {
       suspend fun execute(request: Request): Response

       interface Request {
           // поля запроса
       }

       interface Response {
           // поля ответа
       }
   }
   ```

   Правила:
   - Один файл — одна операция
   - Единственный метод `execute` с единственным аргументом-Request и единственным результатом-Response
   - Типы Request/Response — вложенные интерфейсы (не data class), чтобы не раскрывать реализацию
   - `suspend fun` для корутинной совместимости
   - Добавить KDoc
   - Класс — `public` (по умолчанию)

2. **Создать репозиторий (интерфейс)** в той же доменной папке (если операция работает с данными):

   ```kotlin
   package miotaxi.aidemo.<domain>

   interface <Entity>Repository {
       suspend fun findAll(): List<Entity>
       data class Entity(...)
   }
   ```

3. **Создать реализацию операции**

   Вариант A — в `operation/` (если нет внешних зависимостей):

   ```kotlin
   package miotaxi.aidemo.<domain>

   @Component
   internal class <OperationName>OperationImpl(
       private val repository: <Entity>Repository,
   ) : <OperationName>Operation {
       override suspend fun execute(request: <OperationName>Operation.Request): <OperationName>Operation.Response {
           // бизнес-логика
       }

       private data class <ResponseImpl>(
           override val ...
       ) : <OperationName>Operation.Response
   }
   ```

   Вариант B — в модуле-адаптере (например, `lang-chain/`), когда реализация использует внешнюю технологию.

4. **Создать unit-тесты операции** в `operation/src/test/kotlin/miotaxi/aidemo/<domain>/`

   ```kotlin
   class <OperationName>OperationImplTest {
       private val repository = mockk<<Entity>Repository>()
       private val operation = <OperationName>OperationImpl(repository)

       @Test
       fun `some test case`() = runTest {
           coEvery { repository.someMethod() } returns expectedValue
           val request = object : <OperationName>Operation.Request { ... }
           val result = operation.execute(request)
           assertTrue(result.success)
       }
   }
   ```

   Правила тестов:
   - `@Test`, `runTest { }` для корутин
   - `coEvery` / `coVerify` для suspend-mockk
   - Имена тестов в обратных кавычках, осмысленные

5. **Реализовать репозиторий в `postgres/`** (если нужна БД)

   - `Postgres<Entity>Repository` — `@Component @Profile("pg","h2")`, implements `<Entity>Repository`
   - `CrudRepository` — Spring Data, extends `CoroutineCrudRepository`
   - `Entity` — `@Table`, `@Id`, поля-колонки
   - Тесты — с генераторами и `@DataR2dbcTest` (см. существующие QuestionGenerator, AnswerGenerator)

6. **Проверить ArchUnit тесты** — новая операция должна удовлетворять правилам видимости и связности

---

## 3. Добавление нового HTTP API

Добавляется как контроллер в модуль `web-api`.

### Последовательность шагов

1. **Создать контроллер** в `web-api/src/main/kotlin/miotaxi/aidemo/<domain>/`

   ```kotlin
   package miotaxi.aidemo.<domain>

   @RestController
   @RequestMapping("/api/v1")
   @Tag(name = "<Domain>", description = "API для ...")
   internal class <Verb><Domain>Controller(
       private val <operationName>Operation: <OperationName>Operation,
   ) {
       @<HttpMethod>("/<path>")
       @Operation(summary = "...", description = "...")
       @ApiResponses(ApiResponse(responseCode = "200", description = "Успешный ответ"))
       suspend fun <methodName>(@RequestBody request: <RequestDto>): <ResponseDto> =
           <operationName>Operation.execute(request.toOperationRequest()).toResponse()
   }
   ```

   Правила:
   - Контроллер — `internal class`
   - `@Tag`, `@Operation`, `@ApiResponses` — OpenAPI/Swagger документация
   - DTO (Request/Response data class) — `internal`
   - Мапперы (`toOperationRequest()`, `toResponse()`) — `private` extension functions
   - Request DTO маппится через анонимный объект (`object : <OperationName>.Request { ... }`)

2. **Добавить endpoint в `SecurityConfig.kt`**

   ```kotlin
   .pathMatchers("/api/v1/<path>").permitAll()
   ```

3. **Добавить mock операции в `web-api/src/test/kotlin/miotaxi/aidemo/TestConfig.kt`**

   ```kotlin
   @Bean @Primary
   internal fun <operationName>Operation(): <OperationName>Operation = mockk()
   ```

4. **Создать WebFlux-тесты**

   Класс extends `BaseWebTest`, аннотирован `@WebFluxTest(controllers = [<Controller>::class])` и `@Import(TestConfig::class)`.

   ```kotlin
   @WebFluxTest(controllers = [<Controller>::class])
   @Import(TestConfig::class)
   internal class <Verb><Domain>Test : BaseWebTest() {
       @Autowired
       private lateinit var <operationName>Operation: <OperationName>Operation

       override fun sut(): WebTestClient.ResponseSpec =
           webTestClient.<httpMethod>()
               .uri("/api/v1/<path>")
               .contentType(MediaType.APPLICATION_JSON)
               .bodyValue("""{"field": "value"}""")
               .exchange()

       @Test
       fun `returns 200 with expected response`() {
           coEvery { <operationName>Operation.execute(any()) } returns
               object : <OperationName>Operation.Response { ... }

           sut().expectOk().json("""{"field": "value"}""")
       }

       @Test
       fun `passes request body to operation`() {
           val captured = slot<<OperationName>Operation.Request>()
           coEvery { <operationName>Operation.execute(capture(captured)) } returns
               object : <OperationName>Operation.Response { ... }

           sut().expectOk()

           assert(captured.captured.<field> == "expected")
       }

       @Test
       fun `returns 500 when operation throws`() {
           coEvery { <operationName>Operation.execute(any()) } throws RuntimeException("fail")

           sut().expectStatus().is5xxServerError
       }
   }
   ```

   - `sut()` возвращает `ResponseSpec`
   - `slot` для захвата аргумента и проверки маппинга
   - helper-функции `successTestResponse`/`errorTestResponse` для создания Response (см. `PostNaturalLanguageCommandTest`)

5. **Обновить OpenAPI spec** в `docs/api/openapi.yml` (если поддерживается вручную)

6. **Проверить прохождение тестов**

   ```bash
   ./gradlew :web-api:test
   ```

---

## Сквозная проверка (Definition of Done)

После выполнения любой из задач:

```bash
# Сборка + тесты + линтеры
./gradlew build
./gradlew lint
```

Убедиться, что:
- `./gradlew build` успешен (все тесты проходят)
- `./gradlew lint` без ошибок (detekt + ktlint)
- ArchUnit тесты проходят
- Frontend тесты и линтеры не сломаны
