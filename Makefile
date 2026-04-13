.PHONY: help dev demo release commit

GIT := git
GRADLEW := ./gradlew
COMPOSE := docker compose

help: ## Показать список задач
	@grep -E '^[a-zA-Z0-9_-]+:.*?## ' $(MAKEFILE_LIST) \
		| awk 'BEGIN { FS = ":.*?## " }; { printf "\033[36m%-20s\033[0m %s\n", $$1, $$2 }'

dev: ## Режим разработки: watch за артефактами и авторестарт
	$(GRADLEW) --stop #изредка это стоит делать, иначе жрет оперативку и не возвращает
	$(GRADLEW) build
	touch ./app/build/libs/app-0.0.1-SNAPSHOT.jar
	$(COMPOSE) up -d backend frontend
	@echo "Запущен watch режим. Нажмите Ctrl+C для остановки."
	@echo "Для пересборки backend: ./gradlew :app:jar :app:copyDependencies && touch ./app/build/libs/app-0.0.1-SNAPSHOT.jar"
	@$(COMPOSE) watch || $(COMPOSE) down

demo: ## Запустить сборку в докере, а затем приложение в докере
	$(COMPOSE) -f docker-compose.builder.yaml run builder
	$(COMPOSE) up --build

commit:
	@echo "===Очистка==="
	$(COMPOSE) down --volumes --remove-orphans 2>/dev/null || true
	@echo "===Линтеры==="
	$(GRADLEW) build
	$(GRADLEW) fix
	#(GRADLEW) lint
	@echo "===Тесты==="
	$(GRADLEW) :impactTest --staged
	@echo "===Сборка==="
	$(GRADLEW) :app:jar :app:copyDependencies :front:tarDist
	@echo "===Смоук-тесты в докере==="
	$(COMPOSE) up --build smoke-test
	mkdir -p build/reports/
	docker cp aidemo-smoke-test:/app/build/reports/ui-test/ build/reports/
	$(COMPOSE) down --volumes --remove-orphans
	msg="$$(qwen -p 'проанализируй staged изменения и верни только одну строку - сообщение для коммита без лишних кавычек, без рассуждений и пояснений' -y)" && \
	if whiptail --yesno "$$msg\n\nКоммит, пуш?" 20 80; then \
		$(GIT) commit -m "$$msg"; \
		$(GIT) push; \
	fi
