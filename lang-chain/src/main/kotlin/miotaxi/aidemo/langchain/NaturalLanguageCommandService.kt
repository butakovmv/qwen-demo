package miotaxi.aidemo.langchain

import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage

/**
 * AI-сервис интерпретации команд на естественном языке.
 *
 * Принимает текст команды от пользователя, с помощью LLM определяет, какую из
 * доступных операций (получить вопросы / сохранить ответы) нужно выполнить,
 * вызывает её и возвращает финальный ответ в виде строки (JSON или текст ошибки).
 */
interface NaturalLanguageCommandService {
    @SystemMessage(
        """
        Определи, что хочет пользователь, и ответь строго в одном из форматов:
        - GET_QUESTIONS
        - SEND_ANSWERS: [{"id":"...","text":"..."}]
        - ERROR: <причина>

        Примеры:
        Пользователь: покажи вопросы
        Ответ: GET_QUESTIONS

        Пользователь: вопрос 1: ответ 1
        Ответ: ERROR: не хватает идентификаторов вопросов

        Пользователь: сохрани ответы: id1 -> ответ1, id2 -> ответ2
        Ответ: SEND_ANSWERS: [{"id":"id1","text":"ответ1"},{"id":"id2","text":"ответ2"}]
        """,
    )
    @UserMessage("Команда пользователя: {{it}}")
    fun interpret(command: String): String
}
