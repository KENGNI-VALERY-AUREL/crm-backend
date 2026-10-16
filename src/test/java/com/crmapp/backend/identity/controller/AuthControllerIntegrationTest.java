package com.crmapp.backend.identity.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.crmapp.backend.identity.dto.LoginRequest;
import com.crmapp.backend.identity.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end test of /auth/register and /auth/login: real HTTP layer, real
 * validation, real password hashing, real Flyway migration against a
 * throwaway PostgreSQL container — nothing mocked.
 * <p>
 * Requires Docker Desktop running (Testcontainers starts its own container;
 * "docker compose up -d" is NOT needed for this test — it's a separate,
 * disposable database).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_withNewEmail_returns201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Test SARL", "Ken", "Test", "new-user@test.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully."))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        registerUser("dup@test.com");

        RegisterRequest duplicate = new RegisterRequest(
                "Another Company", "Someone", "Else", "dup@test.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "An account with email 'dup@test.com' already exists."));
    }

    @Test
    void register_withInvalidPayload_returns400() throws Exception {
        RegisterRequest invalid = new RegisterRequest("", "", "", "not-an-email", "short");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_withCorrectCredentials_returns200AndToken() throws Exception {
        registerUser("login-ok@test.com");

        LoginRequest request = new LoginRequest("login-ok@test.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful."))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        registerUser("login-bad@test.com");

        LoginRequest request = new LoginRequest("login-bad@test.com", "wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    private void registerUser(String email) throws Exception {
        RegisterRequest request = new RegisterRequest("Test SARL", "Ken", "Test", email, "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

}
