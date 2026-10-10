package com.crmapp.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the Spring context starts and the Flyway migration succeeds
 * (requires a local PostgreSQL instance — run "docker compose up -d" first,
 * see README).
 */
@SpringBootTest
class CrmBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
