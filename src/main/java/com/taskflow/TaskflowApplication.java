package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TaskFlow Application Entry Point
 *
 * Run with:
 *   ./mvnw spring-boot:run          (dev profile, H2 database)
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod   (prod profile, MySQL)
 *
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * H2 Console: http://localhost:8080/h2-console  (dev only)
 */
@SpringBootApplication
public class TaskflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskflowApplication.class, args);
    }
}