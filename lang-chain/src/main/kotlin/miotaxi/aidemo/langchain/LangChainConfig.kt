package miotaxi.aidemo.langchain

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Конфигурация модуля lang-chain.
 *
 * Подключает ChatModel для взаимодействия с LLM через OpenAI-совместимый API
 * (OpenAI, YandexGPT, GigaChat-proxy, Ollama и т.д.).
 */
@Configuration
internal class LangChainConfig {
    @Bean
    @ConditionalOnMissingBean(ChatModel::class)
    fun chatModel(
        @Value("\${langchain.openai.base-url:https://api.openai.com/v1}") baseUrl: String,
        @Value("\${langchain.openai.api-key:}") apiKey: String,
        @Value("\${langchain.openai.model-name:gpt-4o-mini}") modelName: String,
        @Value("\${langchain.openai.temperature:0.0}") temperature: Double,
        @Value("\${langchain.openai.timeout-seconds:60}") timeoutSeconds: Long,
    ): ChatModel =
        OpenAiChatModel.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .modelName(modelName)
            .temperature(temperature)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .logRequests(false)
            .logResponses(false)
            .build()
}
