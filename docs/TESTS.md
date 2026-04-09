# Тестирование

## Тактика тестирования

| Модуль | Что проверяем | Уровень | Инструменты |
|---|---|---|---|
| `operation/` | Логика use-case'ов (GetQuestions, SendAnswers) | Модульное | JUnit 5, kotlinx-coroutines-test, MockK |
| `postgres/` | R2DBC-репозитории: чтение вопросов, сохранение ответов | Интеграционное | H2 (in-memory), `BasePostgresTest`, генераторы данных |
| `web-api/` | Обработка HTTP-запросов: статус, тело, ошибки контроллеров | Ограниченное интеграционное | `@WebFluxTest`, `WebTestClient`, MockK |
| `app/` | Корректность сборки Spring DI и контекста | Интеграционное | `@SpringBootTest`, MockK |
| `front/` | Компоненты Vue, fetch-запросы, состояния UI | Компонентов | Vitest, `@vue/test-utils`, jsdom |
| `arch-tests/` | Соглашения видимости, связность, зацепление, разрешённые и запрещённые связи между модулями | Архитектурные | ArchUnit, JUnit 5 |
| `ui-test/` | Smoke-тесты: доступность страниц, служебного API, Swagger/OpenAPI | E2E smoke | Playwright (Chromium), Docker |

## Профили Spring

| Профиль | Хранилище | Описание |
|---|---|---|
| *(default)* | — | Нет репозиториев; бины репозиториев предоставляются через моки в тестах |
| `pg` | PostgreSQL (production) | R2DBC-репозитории к PostgreSQL |
| `h2` | H2 (тестовый) | R2DBC-репозитории к H2 in-memory базе (только для тестов `postgres/`) |

## Генераторы тестовых данных

Тесты `postgres/` используют генераторы, которые создают случайные сущности и сразу сохраняют их в БД:

- **`QuestionGenerator`** — генерирует `QuestionEntity` и вставляет через `INSERT INTO`
- **`AnswerGenerator`** — генерирует `AnswerEntity` для заданных вопросов и вставляет

Каждый тест вызывает `createAndInsert()` в `@BeforeEach`, а `@AfterEach` очищает все таблицы.

## Запуск тестов

```bash
# Все тесты
./gradlew test

# Отдельные модули
./gradlew :operation:test
./gradlew :postgres:test
./gradlew :web-api:test
./gradlew :app:test
./gradlew :arch-tests:test

# Frontend
cd front && npm test
```

## Запуск с PostgreSQL

```bash
# С PostgreSQL
docker run --name aidemo-pg -e POSTGRES_DB=aidemo -e POSTGRES_USER=aidemo \
  -e POSTGRES_PASSWORD=aidemo -p 5432:5432 -d postgres:16-alpine
./gradlew :app:bootRun --args='--spring.profiles.active=pg'

# Полный стек в Docker (PostgreSQL + backend + frontend)
make start
```

## Очистка БД после тестов

После прогона тестов `postgres/` автоматически удаляются временные файлы H2-базы (`r2dbc:h2:mem:`) через задачу `doLast` в `tasks.test`.
