package ru.fortushin;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Базовый класс для всех интеграционных тестов.
 * Поднимает реальный PostgreSQL 18 в Testcontainers — тесты гоняются на том же движке, что и прод.
 */
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;


@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("orm_test")
                    .withUsername("test")
                    .withPassword("test");

    static { POSTGRES.start(); }

}