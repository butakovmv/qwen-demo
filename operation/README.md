# Модуль operation

## Назначение
Интерфейсы операций (Use Cases) и их реализации. Содержит бизнес-логику приложения,
отделённую от инфраструктуры и механизмов доставки (HTTP, CLI, MQ и т.д.).

## Содержание
```
src/main/kotlin/miotaxi.aidemo/
├── *Operation.kt              ← интерфейс операции (публичный)
│     interface *Operation
│       interface Request?          ← аргументы (data class/interface), может отсутствовать
│       interface Response          ← результат (interface)
│       suspend fun execute(): Response
│
└── *OperationImpl.kt          ← реализация (internal)
      @Component internal class *OperationImpl
        data class *ResponseImpl : Response
```
## Подход к расположению

### Интерфейс операции (`*Operation.kt`)
В одном файле находятся:
- **`Request`** — тип аргументов (обычно `data class` или `interface`). Может отсутствовать, если операция без параметров.
- **`Response`** — интерфейс результата. Реализации — в файле реализации.
- **`execute()`** — сигнатура метода выполнения, допустимы и иные имена, обозначающие действия - get, list, find, update, remove и т.д.

### Реализация операции (`*OperationImpl.kt`)
В одном файле находятся:
- **`@Component`-класс** — реализация интерфейса операции, инъекция зависимостей.
- **`*Impl` data class** — конкретная реализация `Response`, создаваемая внутри операции.

### Почему так
| Артефакт | Где | Почему |
|----------|-----|--------|
| `Request` | Интерфейс операции | Тип аргументов — часть контракта, нужен потребителю |
| `Response` (interface) | Интерфейс операции | Тип результата — часть контракта, нужен потребителю |
| `*Impl` (data class) | Реализация операции | Деталь реализации, не уходит за пределы модуля |
| `@Component` | Реализация операции | Spring-компонент, регистрируется в DI-контексте |

Потребители (контроллеры, CLI-обработчики) зависят только от интерфейса —
`Request`, `Response`, `execute()`. Реализации инкапсулированы в `*Impl`-файле.


## Языки и фреймворки

| Категория | Технология | Версия |
|---|---|---|
| Язык | Kotlin | 2.1.10 |
| JVM-плагин | kotlin("jvm") | 2.1.10 |
| Spring-плагин | kotlin("plugin.spring") | 2.1.10 |
| Kotlinx Coroutines Core | kotlinx-coroutines-core | 1.9.0 |
| Spring Context | spring-context | 6.2.1 |
| Тестовый фреймворк | JUnit 5 | 5.11.0 |
| Coroutines Test | kotlinx-coroutines-test | 1.9.0 |
| ktlint | org.jlleitschuh.gradle.ktlint | 12.3.0 |

## Зависимости
- **Не зависит** от других модулей проекта (самый нижний слой)
- **Зависимости (runtime):** `kotlin-stdlib`, `kotlinx-coroutines-core`, `spring-context` (только аннотации, без Boot)
- **Зависимости (тесты):** `kotlinx-coroutines-test`, `junit-jupiter`

## Связи
Не зависит от прочих модулей. Используется модулями `web-api` (контроллеры вызывают Use Case) и `app` (composition root). Зависит только от стандартной библиотеки Kotlin, coroutines и Spring Context (для `@Component`).

## Сборка
Собирается в JAR-библиотеку через `./gradlew :operation:assemble`. Не является самостоятельным приложением — подключается как зависимость в `web-api` и `app`.

## Тесты
Юнит-тесты бизнес-логики на JUnit 5 + `kotlinx-coroutines-test` (`runTest`). Тесты проверяют корректность выполнения операций и структуру ответа. Запуск: `./gradlew :operation:test`.
