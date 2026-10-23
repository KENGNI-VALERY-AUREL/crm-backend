package com.crmapp.backend.identity.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.crmapp.backend.identity.dto.LoginRequest;
import com.crmapp.backend.identity.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
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
 * UC-201 (host manages members), UC-202 (reassign a prospect), UC-301 (super
 * admin structures the company). Requires Docker, like the other integration tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class TeamManagementIntegrationTest {

    private static final String PASSWORD = "password123";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void superAdmin_createsGroup_andListsBothGroups_duplicateNameRefused() throws Exception {
        String admin = register("team-groups@test.com");

        mockMvc.perform(post("/groups").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Douala team\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Douala team"))
                .andExpect(jsonPath("$.data.userCount").value(0));

        mockMvc.perform(get("/groups").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(post("/groups").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"douala TEAM\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void host_createsMembersInOwnGroupOnly() throws Exception {
        String admin = register("team-host-admin@test.com");
        String defaultGroupId = groupIdByName(admin, "Default");
        String doualaId = createGroup(admin, "Douala team");

        createUser(admin, "host1@test.com", "HOST", doualaId).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("HOST"))
                .andExpect(jsonPath("$.data.groupId").value(doualaId));
        String host = login("host1@test.com");

        // No groupId: defaults to the host's own group.
        createUser(host, "member1@test.com", "MEMBER", null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.groupId").value(doualaId));

        // A host cannot create another host, nor reach another group.
        createUser(host, "host2@test.com", "HOST", null).andExpect(status().isForbidden());
        createUser(host, "member2@test.com", "MEMBER", defaultGroupId).andExpect(status().isNotFound());

        // Nobody creates a SUPER_ADMIN through the API; duplicate emails are refused.
        createUser(admin, "boss@test.com", "SUPER_ADMIN", null).andExpect(status().isBadRequest());
        createUser(admin, "member1@test.com", "MEMBER", null).andExpect(status().isConflict());

        // The host only lists their own group (host + member).
        mockMvc.perform(get("/users").header("Authorization", bearer(host)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // The group now shows its host.
        mockMvc.perform(get("/groups").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.data[?(@.name=='Douala team')].hosts.length()").value(1));
    }

    @Test
    void member_cannotManageUsersOrGroups() throws Exception {
        String admin = register("team-member-admin@test.com");
        createUser(admin, "plain-member@test.com", "MEMBER", null).andExpect(status().isCreated());
        String member = login("plain-member@test.com");

        mockMvc.perform(get("/users").header("Authorization", bearer(member))).andExpect(status().isForbidden());
        mockMvc.perform(get("/groups").header("Authorization", bearer(member))).andExpect(status().isForbidden());
        createUser(member, "x@test.com", "MEMBER", null).andExpect(status().isForbidden());
    }

    @Test
    void superAdmin_promotesMemberToHost_hostCannotChangeRoles() throws Exception {
        String admin = register("team-role-admin@test.com");
        String memberId = createUserId(admin, "to-promote@test.com", "MEMBER");

        mockMvc.perform(patch("/users/" + memberId + "/role").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"HOST\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("HOST"));

        String newHost = login("to-promote@test.com");
        mockMvc.perform(patch("/users/" + memberId + "/role").header("Authorization", bearer(newHost))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"MEMBER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void host_reassignsProspect_toMemberOfSameGroupOnly() throws Exception {
        String admin = register("team-reassign@test.com");
        String otherGroupId = createGroup(admin, "Yaounde team");
        String memberId = createUserId(admin, "assignee@test.com", "MEMBER");
        createUser(admin, "outsider@test.com", "MEMBER", otherGroupId).andExpect(status().isCreated());
        String outsiderId = userIdByEmail(admin, "outsider@test.com");

        String prospectId = createProspect(admin, "Alice");

        mockMvc.perform(patch("/prospects/" + prospectId + "/assignee").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedUserId").value(memberId));

        // The member now sees it in their own list.
        String member = login("assignee@test.com");
        mockMvc.perform(get("/prospects").header("Authorization", bearer(member)))
                .andExpect(jsonPath("$.data.length()").value(1));

        // A user of another group cannot receive it.
        mockMvc.perform(patch("/prospects/" + prospectId + "/assignee").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + outsiderId + "\"}"))
                .andExpect(status().isNotFound());

        // A member cannot reassign.
        mockMvc.perform(patch("/prospects/" + prospectId + "/assignee").header("Authorization", bearer(member))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + memberId + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void removingUser_isRefusedWhileProspectsAssigned_thenWorks_andEmailCanBeReused() throws Exception {
        String admin = register("team-delete@test.com");
        String memberId = createUserId(admin, "leaver@test.com", "MEMBER");
        String prospectId = createProspect(admin, "Bob");
        mockMvc.perform(patch("/prospects/" + prospectId + "/assignee").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + memberId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/" + memberId).header("Authorization", bearer(admin)))
                .andExpect(status().isConflict());

        String adminId = userIdByEmail(admin, "team-delete@test.com");
        mockMvc.perform(patch("/prospects/" + prospectId + "/assignee").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"" + adminId + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/" + memberId).header("Authorization", bearer(admin)))
                .andExpect(status().isOk());

        // The removed user can no longer log in...
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("leaver@test.com", PASSWORD))))
                .andExpect(status().isUnauthorized());

        // ...and their email is free again (partial unique index, V4).
        createUser(admin, "leaver@test.com", "MEMBER", null).andExpect(status().isCreated());

        // Nobody removes themselves.
        mockMvc.perform(delete("/users/" + adminId).header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- helpers

    private String register(String email) throws Exception {
        RegisterRequest request = new RegisterRequest("Test SARL", "Ken", "Test", email, PASSWORD);
        String body = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/token").asText();
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/token").asText();
    }

    private org.springframework.test.web.servlet.ResultActions createUser(
            String token, String email, String role, String groupId) throws Exception {
        String groupPart = groupId == null ? "" : ",\"groupId\":\"" + groupId + "\"";
        return mockMvc.perform(post("/users").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Fn\",\"lastName\":\"Ln\",\"email\":\"" + email
                        + "\",\"password\":\"" + PASSWORD + "\",\"role\":\"" + role + "\"" + groupPart + "}"));
    }

    private String createUserId(String token, String email, String role) throws Exception {
        String body = createUser(token, email, role, null).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/id").asText();
    }

    private String createGroup(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/groups").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/id").asText();
    }

    private String groupIdByName(String token, String name) throws Exception {
        String body = mockMvc.perform(get("/groups").header("Authorization", bearer(token)))
                .andReturn().getResponse().getContentAsString();
        for (JsonNode group : objectMapper.readTree(body).at("/data")) {
            if (name.equals(group.get("name").asText())) {
                return group.get("id").asText();
            }
        }
        throw new AssertionError("Group not found: " + name);
    }

    private String userIdByEmail(String token, String email) throws Exception {
        String body = mockMvc.perform(get("/users").header("Authorization", bearer(token)))
                .andReturn().getResponse().getContentAsString();
        for (JsonNode user : objectMapper.readTree(body).at("/data")) {
            if (email.equals(user.get("email").asText())) {
                return user.get("id").asText();
            }
        }
        throw new AssertionError("User not found: " + email);
    }

    private String createProspect(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/prospects").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/id").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

}
