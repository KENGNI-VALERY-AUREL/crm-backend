package com.crmapp.backend.pipeline.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.crmapp.backend.identity.dto.LoginRequest;
import com.crmapp.backend.identity.dto.RegisterRequest;
import com.crmapp.backend.identity.enums.UserRole;
import com.crmapp.backend.identity.model.User;
import com.crmapp.backend.identity.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end test of the pipeline module (default pipeline, Kanban move,
 * stage history, RBAC, tenant isolation). Requires Docker, like
 * AuthControllerIntegrationTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class PipelineIntegrationTest {

    private static final String PASSWORD = "password123";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_createsDefaultPipelineWithFiveStages() throws Exception {
        String token = register("pipe-default@test.com");

        mockMvc.perform(get("/pipelines").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].stages.length()").value(5))
                .andExpect(jsonPath("$.data[0].stages[0].name").value("New"))
                .andExpect(jsonPath("$.data[0].stages[3].outcome").value("WON"))
                .andExpect(jsonPath("$.data[0].stages[4].outcome").value("LOST"));
    }

    @Test
    void newProspect_startsOnFirstStage_andMoveIsRecordedInHistory() throws Exception {
        String token = register("pipe-move@test.com");
        JsonNode pipeline = firstPipeline(token);
        String firstStageId = pipeline.at("/stages/0/id").asText();
        String contactedStageId = pipeline.at("/stages/1/id").asText();

        String prospectId = createProspect(token, "Alice");
        mockMvc.perform(get("/prospects/" + prospectId).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data.currentStageId").value(firstStageId));

        mockMvc.perform(patch("/prospects/" + prospectId + "/stage")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stageId\":\"" + contactedStageId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStageId").value(contactedStageId));

        mockMvc.perform(get("/prospects/" + prospectId + "/stage-history")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].fromStageId").value(firstStageId))
                .andExpect(jsonPath("$.data[0].toStageId").value(contactedStageId));

        // The board shows the prospect in its new column.
        mockMvc.perform(get("/pipelines/" + pipeline.get("id").asText() + "/board")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.columns[0].prospects.length()").value(0))
                .andExpect(jsonPath("$.data.columns[1].prospects.length()").value(1));
    }

    @Test
    void moveToLostStage_requiresAReason() throws Exception {
        String token = register("pipe-lost@test.com");
        String lostStageId = firstPipeline(token).at("/stages/4/id").asText();
        String prospectId = createProspect(token, "Bob");

        mockMvc.perform(patch("/prospects/" + prospectId + "/stage")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stageId\":\"" + lostStageId + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A reason is required when moving a prospect to a lost stage."));

        mockMvc.perform(patch("/prospects/" + prospectId + "/stage")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stageId\":\"" + lostStageId + "\",\"reason\":\"Chose a competitor\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void hostCanManageStages_deleteIsRefusedWhileStageHoldsProspects() throws Exception {
        String token = register("pipe-config@test.com");
        JsonNode pipeline = firstPipeline(token);
        String pipelineId = pipeline.get("id").asText();
        String firstStageId = pipeline.at("/stages/0/id").asText();

        // Insert a new stage at index 1: "Contacted" and the following stages shift down.
        mockMvc.perform(post("/pipelines/" + pipelineId + "/stages")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Qualified\",\"sortOrder\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stages.length()").value(6))
                .andExpect(jsonPath("$.data.stages[1].name").value("Qualified"))
                .andExpect(jsonPath("$.data.stages[2].name").value("Contacted"))
                .andExpect(jsonPath("$.data.stages[5].sortOrder").value(5));

        createProspect(token, "Carol"); // lands on the first stage ("New")

        mockMvc.perform(delete("/pipelines/" + pipelineId + "/stages/" + firstStageId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
    }

    @Test
    void member_cannotConfigureStages_norSeeOthersProspects() throws Exception {
        String ownerEmail = "pipe-owner@test.com";
        String ownerToken = register(ownerEmail);
        String pipelineId = firstPipeline(ownerToken).get("id").asText();
        String prospectId = createProspect(ownerToken, "Dave");

        String memberToken = createMemberInSameGroup(ownerEmail, "pipe-member@test.com");

        mockMvc.perform(post("/pipelines/" + pipelineId + "/stages")
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nope\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Prospect assigned to the owner: invisible (404) to the member, even in the same group.
        mockMvc.perform(get("/prospects/" + prospectId).header("Authorization", bearer(memberToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherOrganization_cannotUseForeignStageOrPipeline() throws Exception {
        String tokenA = register("pipe-org-a@test.com");
        JsonNode pipelineA = firstPipeline(tokenA);
        String stageOfA = pipelineA.at("/stages/1/id").asText();

        String tokenB = register("pipe-org-b@test.com");
        String prospectOfB = createProspect(tokenB, "Eve");

        mockMvc.perform(patch("/prospects/" + prospectOfB + "/stage")
                        .header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stageId\":\"" + stageOfA + "\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/pipelines/" + pipelineA.get("id").asText() + "/board")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- helpers

    private String register(String email) throws Exception {
        RegisterRequest request = new RegisterRequest("Test SARL", "Ken", "Test", email, PASSWORD);
        return tokenFrom(mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    private String createMemberInSameGroup(String ownerEmail, String memberEmail) throws Exception {
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow();
        User member = new User();
        member.setGroup(owner.getGroup());
        member.setFirstName("Mem");
        member.setLastName("Ber");
        member.setEmail(memberEmail);
        member.setPasswordHash(passwordEncoder.encode(PASSWORD));
        member.setRole(UserRole.MEMBER);
        userRepository.save(member);

        LoginRequest login = new LoginRequest(memberEmail, PASSWORD);
        return tokenFrom(mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))));
    }

    private String tokenFrom(ResultActions result) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/token").asText();
    }

    private JsonNode firstPipeline(String token) throws Exception {
        String body = mockMvc.perform(get("/pipelines").header("Authorization", bearer(token)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/0");
    }

    private String createProspect(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/prospects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/id").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

}
