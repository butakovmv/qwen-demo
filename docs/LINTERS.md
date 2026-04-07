# Линтеры и форматтеры


## Запуск

| Задача | Что делает |
|---|---|
| `./gradlew lint` | Все линтеры (detekt + ktlint + ESLint + Prettier) |
| `./gradlew fix` | Автоисправление (ktlint + ESLint + Prettier) |

## Инструменты

| Инструмент | Модули | Конфиг | Правила |
|---|---|---|---|
| **detekt** | `usecase`, `web-api`, `app`, `arch-tests` | `config/detekt/detekt.yml` | Kotlin: магические числа, сложность, null safety, именование, unused imports, длина строки ≤ 120 |
| **ktlint** | `usecase`, `web-api`, `app`, `arch-tests` | По умолчанию (Kotlin official style) | Форматирование: импорты, trailing comma, пустые строки, отступы |
| **ESLint** | `front/` | `front/eslint.config.mjs` | TypeScript/Vue: unused vars, типы, `vue/multi-word-component-names` отключён |
| **Prettier** | `front/` | `front/.prettierrc` | Форматирование: singleQuote, trailingComma: es5, printWidth: 100 |
| **ArchUnit** | `arch-tests/` | — (код в тестах) | Области видимости, coupling между модулями, cohesion, зависимости пакетов |