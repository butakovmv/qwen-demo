# Тестирование

## Запуск

| Команда | Что запускает |
|---|---|
| `./gradlew test` | Все тесты (backend, frontend, archunit) |
| `./gradlew :usecase:test` | Только usecase-тесты |
| `./gradlew :web-api:test` | Только API-тесты |
| `./gradlew :arch-tests:test` | Только ArchUnit |
| `./gradlew :front:test` | Только фронтенд-тесты |


## Уровни тестирования

| Уровень | Модуль | Инструменты | Что проверяет |
|---|---|---|---|
| **Unit** | `usecase/` | JUnit 5, Mocks | Логика вариантов использования |
| **API** | `web-api/` | `@WebFluxTest`, `WebTestClient`, `@MockitoBean` | Контроллеры: статус, тело, валидация |
| **Интеграция** | `postgres/` | `@DataR2dbcTest` | Работа с БД |
| **Интеграция** | `app/` | Тесты контекста Spring | Корректность сборки DI |
| **Фронтенд** | `front/` | Vitest, @vue/test-utils, jsdom | Компоненты Vue, fetch, состояния |
| **Архитектура** | `arch-tests/` | ArchUnit | Соглашения видимости, coupling, cohesion, разрешенные и запрещенные связи |