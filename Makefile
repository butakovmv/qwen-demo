.PHONY: help dev demo release  install-hooks

GIT := git
GRADLEW := ./gradlew
COMPOSE := docker compose

help: ## Показать список задач
	@grep -E '^[a-zA-Z0-9_-]+:.*?## ' $(MAKEFILE_LIST) \
		| awk 'BEGIN { FS = ":.*?## " }; { printf "\033[36m%-20s\033[0m %s\n", $$1, $$2 }'

dev: ## Режим разработки: watch за артефактами и авторестарт
	$(GRADLEW) --stop #изредка это стоит делать, иначе жрет оперативку и не возвращает
	$(GRADLEW) :app:bootJar :front:tarDist
	touch ./app/build/libs/app-0.0.1-SNAPSHOT.jar
	$(COMPOSE) up -d backend frontend
	@echo "Запущен watch режим. Нажмите Ctrl+C для остановки."
	@echo "Для пересборки backend: ./gradlew :app:bootJar && touch ./app/build/libs/app-0.0.1-SNAPSHOT.jar"
	@$(COMPOSE) watch || $(COMPOSE) down

demo: ## Запустить сервисы (пересборка)
	$(GRADLEW) :app:bootJar :front:tarDist
	$(COMPOSE) up --build

commit:
	@echo "===Очистка==="
	$(COMPOSE) down --volumes --remove-orphans 2>/dev/null || true
	$(GRADLEW) clean
	@echo "===Линтеры==="
	$(GRADLEW) fix
	#(GRADLEW) lint
	@echo "===Тесты==="
	$(GRADLEW) :impactTest --staged
	@echo "===Сборка==="
	$(GRADLEW) :app:bootJar :front:tarDist
	@echo "===Смоук-тесты в докере==="
	$(COMPOSE) up --build smoke-test
	mkdir -p build/reports/
	docker cp aidemo-smoke-test:/app/build/reports/ui-test/ build/reports/
	$(COMPOSE) down --volumes --remove-orphans
	#git commit -m "$(qwen -p 'проанализируй изменения и верни только одну строку - сообщение для коммита, без рассуждений и пояснений' -y)"

install-hooks: ## Установить git pre-commit hook
	cp hooks/pre-commit .git/hooks/pre-commit
	chmod +x .git/hooks/pre-commit
	@echo "Pre-commit hook installed."
