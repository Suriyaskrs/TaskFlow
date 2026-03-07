package com.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context load test — verifies the Spring context starts without errors.
 * Run with: ./mvnw test
 */
@SpringBootTest
@ActiveProfiles("dev")
class TaskflowApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, all beans are wired correctly
    }
}