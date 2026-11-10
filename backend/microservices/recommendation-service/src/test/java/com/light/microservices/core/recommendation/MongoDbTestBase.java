package com.light.microservices.core.recommendation;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

/**
 * Base class for integration tests that need a real MongoDB. A single container is
 * started once for the whole test JVM and its host/port are wired into the Spring
 * context via {@link DynamicPropertySource}, so tests run against a real database
 * without requiring a locally installed MongoDB.
 */
public abstract class MongoDbTestBase {

    private static final MongoDBContainer database = new MongoDBContainer("mongo:6.0.4");

    static {
        database.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", database::getHost);
        registry.add("spring.data.mongodb.port", () -> database.getMappedPort(27017));
        registry.add("spring.data.mongodb.database", () -> "test-db");
    }
}
