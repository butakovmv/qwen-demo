package miotaxi.aidemo.webapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("OTUS Demo API")
                    .version("v1.0")
                    .description("REST API для демонстрационного проекта OTUS")
                    .contact(
                        Contact()
                            .name("OTUS Team"),
                    ),
            )
    }
}
