.PHONY: clean build test lint start stop logs smoke-test ci swagger help install-hooks

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

demo: ## Запустить сервисы (пересборка)
	$(GRADLEW) :app:bootJar :front:tarDist
	$(COMPOSE) up --build

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

dev: ## Режим разработки: watch за артефактами и авторестарт
	$(GRADLEW) lint -t
	$(GRADLEW) :app:bootJar :front:tarDist
	touch ./app/build/libs/app-0.0.1-SNAPSHOT.jar
	$(COMPOSE) up -d backend frontend
	@echo "Запущен watch режим. Нажмите Ctrl+C для остановки."
	@echo "Для пересборки backend: ./gradlew :app:bootJar && touch ./app/build/libs/app-0.0.1-SNAPSHOT.jar"
	@$(COMPOSE) watch || $(COMPOSE) down

install-hooks: ## Установить git pre-commit hook
	cp hooks/pre-commit .git/hooks/pre-commit
	chmod +x .git/hooks/pre-commit
	@echo "Pre-commit hook installed."
