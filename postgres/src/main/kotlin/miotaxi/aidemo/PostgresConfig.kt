package miotaxi.aidemo

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.r2dbc.core.DatabaseClient

/**
 * Конфигурация модуля postgres.
 * Активируется только с профилями `local` или `docker`.
 */
@Configuration
@Profile("pg")
internal class PostgresConfig {
    @Bean
    fun databaseClient(connectionFactory: ConnectionFactory): DatabaseClient = DatabaseClient.create(connectionFactory)

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)

        val populator = CompositeDatabasePopulator()
        populator.addPopulators(
            ResourceDatabasePopulator(ClassPathResource("db/schema.sql")),
        )
        initializer.setDatabasePopulator(populator)

        return initializer
    }
}
