package com.light.microservices.core.review;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base class for integration tests that need a real MySQL. A single container is
 * started once for the whole test JVM and its JDBC url/credentials are wired into
 * the Spring context via {@link DynamicPropertySource}, so tests run against a real
 * database without requiring a locally installed MySQL.
 */
public abstract class MySqlTestBase {

    private static final MySQLContainer<?> database = new MySQLContainer<>("mysql:5.7.32");

    static {
        database.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }
}
