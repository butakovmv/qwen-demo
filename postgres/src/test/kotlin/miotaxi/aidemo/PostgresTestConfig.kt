package miotaxi.aidemo

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

/**
 * Минимальная Spring Boot конфигурация для интеграционных тестов postgres-модуля.
 * Поднимает H2 (r2dbc, in-memory) и инициализирует схему.
 */
@Configuration
@EnableAutoConfiguration
internal class PostgresTestConfig {
    @Bean
    fun connectionFactory(): ConnectionFactory {
        val config =
            io.r2dbc.h2.H2ConnectionConfiguration.Builder()
                .url("r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                .build()
        return io.r2dbc.h2.H2ConnectionFactory(config)
    }

    @Bean
    fun schemaInitializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        return ConnectionFactoryInitializer().apply {
            setConnectionFactory(connectionFactory)
            setDatabasePopulator(
                CompositeDatabasePopulator().apply {
                    addPopulators(
                        ResourceDatabasePopulator(ClassPathResource("db/schema-h2.sql")),
                    )
                },
            )
        }
    }
}
