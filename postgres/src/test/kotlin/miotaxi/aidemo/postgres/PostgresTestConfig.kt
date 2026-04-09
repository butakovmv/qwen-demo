package miotaxi.aidemo.postgres

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import java.nio.file.Paths
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

/**
 * Минимальная Spring Boot конфигурация для интеграционных тестов postgres-модуля.
 * Поднимает H2 (r2dbc) и инициализирует схему.
 */
@Configuration
@EnableAutoConfiguration
internal class PostgresTestConfig {
    private val h2Dir = Paths.get("r2dbc:h2:mem:")

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val config =
            io.r2dbc.h2.H2ConnectionConfiguration.Builder()
                .url("r2dbc:h2:mem://localhost/testdb-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
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

    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    @jakarta.annotation.PreDestroy
    fun cleanup() {
        if (h2Dir.exists() && h2Dir.isDirectory()) {
            h2Dir.deleteRecursively()
        }
    }
}
