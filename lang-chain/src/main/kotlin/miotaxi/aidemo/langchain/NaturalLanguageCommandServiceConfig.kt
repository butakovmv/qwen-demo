package miotaxi.aidemo.langchain

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация AI-сервиса.
 *
 * Регистрирует бин {@link NaturalLanguageCommandService} на основе {@link ChatModel}
 * и подключает к нему инструменты {@link OperationTools}.
 */
@Configuration
internal class NaturalLanguageCommandServiceConfig {
    @Bean
    fun naturalLanguageCommandService(chatModel: ChatModel): NaturalLanguageCommandService =
        AiServices.builder(NaturalLanguageCommandService::class.java)
            .chatModel(chatModel)
            .build()
}
