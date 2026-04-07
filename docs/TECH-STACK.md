# Стек технологий

## Backend

| Технология | Версия | Назначение |
|---|---|---|
| **Kotlin** | 1.9.23 | Основной язык |
| **Spring Boot** | 3.4.1 | Фреймворк (WebFlux, Netty) |
| **Gradle (Kotlin DSL)** | 8.14.1 | Система сборки |
| **Spring WebFlux** | 3.4.1 | Реактивный HTTP |
| **Kotlin Coroutines** | 1.9.0 | Асинхронность (`suspend fun`) |
| **Jackson** | — | Сериализация JSON (`jackson-module-kotlin`) |

### Тестирование

| Зависимость | Назначение |
|---|---|
| `spring-boot-starter-test` | Unit/интеграционные тесты |
| `reactor-test` | Тестирование реактивных потоков |

## Frontend

| Технология | Версия | Назначение |
|---|---|---|
| **Vue 3** | 3.5.13 | Фреймворк (Composition API, `<script setup>`) |
| **TypeScript** | 5.7.2 | Типизированный JS |
| **Vite** | 6.x | Сборка и dev-сервер |
| **vue-tsc** | 2.2.0 | Проверка типов Vue |
| **nginx** | alpine | Раздача статики + proxy на backend |

## Линтеры и анализ кода

### Backend

| Инструмент | Версия | Назначение |
|---|---|---|
| **detekt** | 1.23.6 | Статический анализ Kotlin |
| **ktlint** | 12.1.1 | Форматирование Kotlin-кода |

### Frontend

| Инструмент | Версия | Назначение |
|---|---|---|
| **ESLint** | 10.x | Статический анализ (Flat config) |
| **typescript-eslint** | 8.58 | TypeScript-правила для ESLint |
| **eslint-plugin-vue** | 10.x | Vue SFC правила |
| **Prettier** | 3.8.1 | Форматирование кода |
| **ArchUnit** | 1.3.0 | Проверка архитектурных ограничений |
| **Vitest** | — | Тестирование фронтенда |
| **@vue/test-utils** | — | Утилиты тестирования Vue |
| **jsdom** | — | DOM-окружение для тестов |

Подробная документация: [docs/LINTERS.md](./LINTERS.md)

## Инфраструктура

| Технология | Версия | Назначение |
|---|---|---|
| **Docker Compose** | — | Запуск backend + frontend |
| **Eclipse Temurin JDK** | 21 (Alpine) | Сборка backend |
| **Eclipse Temurin JRE** | 21 (Alpine) | Runtime backend |
| **Node.js** | 22 (Alpine) | Сборка фронтенда |

## Репозитории зависимостей

Приоритет: российские зеркала → центральные

1. [GitVerse Maven](https://gitverse.ru/api/packages/maven)
2. [Сбер Cloud Maven](https://maven.sbercloud.ru/repository/maven-public)
3. [Maven Central](https://repo.maven.apache.org/maven2/)
