# Модуль фронта

## Назначение
Визуальное взаимодействие с пользователем в браузере и обращение к апи бэка по сети

## Содержание
Vue3-компоненты, верстка, стили

## Связи
Изолирован от остальных модулей отличающимися инструментами и языком. Связан через контракт HTTP API с модулем web-api.

## Языки и фреймворки

| Категория | Технология | Версия |
|---|---|---|
| Язык | TypeScript | ~5.7.2 |
| Фреймворк | Vue 3 (Composition API) | ^3.5.13 |
| Сборщик | Vite | ^6.0.0 |
| Тестовый фреймворк | Vitest | ^4.1.2 |
| Тестирование компонентов | Vue Test Utils | ^2.4.6 |
| Среда для тестов | jsdom | ^29.0.1 |
| Линтер | ESLint + eslint-plugin-vue + typescript-eslint + vue-eslint-parser | ^10.x / ^8.58.0 / ^10.4.0 |
| Форматтер | Prettier + eslint-plugin-prettier | ^3.8.1 / ^5.5.5 |
| Type-check | vue-tsc | ^2.2.0 |
| Плагин Vite | @vitejs/plugin-vue | ^5.2.1 |

## Инструменты
- `npm ci` / `npm install` — установка зависимостей
- `npm run dev` — dev-сервер Vite (порт 3000) с проксированием `/api` на `http://localhost:8080`
- `npm run build` — production-сборка с type-check через `vue-tsc`
- `npm run lint:check` / `npm run lint` — проверка и автоисправление ESLint
- `npm run format:check` / `npm run format` — проверка и автоформатирование Prettier
- `npm run test` / `npm run test:watch` — запуск Vitest
- `npm run preview` — просмотр production-сборки через Vite

## Сборка
Через Gradle задача `front:npmBuild` → Vite собирает статику в `dist/`.
Задача `front:tarDist` упаковывает `dist/` в `front-dist.tar`, который монтируется в Nginx-контейнер (Dockerfile: `FROM nginx:alpine`).

## Тесты
Витест (Vitest) с окружением jsdom и глобальным API (`globals: true` в `vite.config.ts`).
Фреймворк Vue Test Utils для рендеринга и проверки компонентов.
Тесты находятся в `src/**/*.spec.ts`.
Запуск: `npm run test` (одноразовый) или `npm run test:watch` (watch-режим).
Через Gradle: `./gradlew :front:test` (требует `npm ci`, задача автоматически зависит от `npmInstall`).