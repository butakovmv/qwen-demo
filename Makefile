.PHONY: clean build test lint start stop logs smoke-test ci swagger help

GRADLEW := ./gradlew
COMPOSE := docker compose

help: ## Показать список задач
	@grep -E '^[a-zA-Z0-9_-]+:.*?## ' $(MAKEFILE_LIST) \
		| awk 'BEGIN { FS = ":.*?## " }; { printf "\033[36m%-20s\033[0m %s\n", $$1, $$2 }'

clean: ## Очистить артефакты
	$(COMPOSE) down --volumes --remove-orphans 2>/dev/null || true
	$(GRADLEW) clean

build: ## Собрать backend и frontend
	$(GRADLEW) :app:bootJar :front:tarDist

test: ## Запустить все тесты
	$(GRADLEW) test

lint: ## Запустить линтеры
	$(GRADLEW) lint
	$(GRADLEW) fix

start: ## Запустить сервисы (пересборка)
	$(GRADLEW) :app:bootJar :front:tarDist
	$(COMPOSE) up --build -d

stop: ## Остановить сервисы
	$(COMPOSE) down

logs: ## Логи контейнеров
	$(COMPOSE) logs -f

smoke-test: ## Поднять всё в Docker → smoke-тесты → остановить
	$(GRADLEW) :app:bootJar :front:tarDist
	$(COMPOSE) up --build smoke-test
	docker cp aidemo-smoke-test:/app/build/reports/ui-test/ build/reports/
	$(COMPOSE) down --volumes --remove-orphans

ci: ## Полный CI-пайплайн: clean → lint → test → smoke-test
	$(MAKE) clean
	$(MAKE) lint
	$(MAKE) test
	$(MAKE) smoke-test

swagger: ## Открыть Swagger UI
	@xdg-open http://localhost:81/swagger-ui.html 2>/dev/null || \
		echo "Откройте: http://localhost:81/swagger-ui.html"
