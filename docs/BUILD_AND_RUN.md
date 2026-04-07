# Сборка и запуск

## Запуск приложения в докере

| Задача | Команда |
|---|---|
| Запустить всё (Docker) | `docker compose up -d` |
| Остановить всё (Docker) | `docker compose down` |
| Пересобрать и запустить | `docker compose up -d --build` |
| Посмотреть логи | `docker compose logs -f` |
| Посмотреть логи backend | `docker compose logs -f backend` |
| Посмотреть логи frontend | `docker compose logs -f frontend` |


## Локальная разработка и отладка

| Задача | Команда |
|---|---|
| Запустить только backend | `./gradlew :app:bootRun` |
| Запустить dev-сервер фронта | `cd front && npm run dev` |
| Предпросмотр сборки фронта | `cd front && npm run preview` |
| Собрать фронт | `cd front && npm run build` |
| Собрать бэк | `./gradlew :app:bootJar` |
| Очистить | `./gradlew clean` |
| Очистить + полная пересборка | `./gradlew clean build` |

## Линтеры и тесты

| Задача | Команда |
|---|---|
| Все линтеры (detekt + ktlint + ESLint + Prettier) | `./gradlew lint` |
| Автоисправление (ktlint + frontend ESLint/Prettier) | `./gradlew fix` |
| Все тесты (backend + frontend) | `./gradlew test` |
| Только frontend-тесты | `./gradlew :front:test` |
| Только ArchUnit | `./gradlew :arch-tests:test` |
| Все проверки (тесты + линтеры) | `./gradlew check` |

## Ссылки

| Что | Где |
|---|---|
| Backend API | `http://localhost:8080` |
| Frontend | `http://localhost:3000` |
| Actuator | `http://localhost:8080/actuator` |


