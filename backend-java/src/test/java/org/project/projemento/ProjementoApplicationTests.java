package org.project.projemento;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjementoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void contextLoads() {
    }

    @Test
    void authFlowWorks() throws Exception {
        mockMvc.perform(post("/api/v1/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "tester",
                                  "email": "tester@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        var loginResult = mockMvc.perform(post("/api/users/login/token")
                        .with(httpBasic("tester@example.com", "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(cookie().exists("refresh-token"))
                .andReturn();

        String accessToken = objectMapper.readValue(
                loginResult.getResponse().getContentAsByteArray(),
                new TypeReference<Map<String, String>>() {
                }
        ).get("accessToken");
        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh-token");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tester@example.com"));

        mockMvc.perform(post("/auth/refresh/token")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString());

        mockMvc.perform(post("/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh-token", 0));
    }

    @Test
    void projectBoardTaskAndCommentFlowWorks() throws Exception {
        Map<String, Object> owner = register("owner", "owner@example.com", "secret123");
        Map<String, Object> member = register("member", "member@example.com", "secret123");
        register("outsider", "outsider@example.com", "secret123");

        String ownerToken = login("owner@example.com", "secret123");
        String memberToken = login("member@example.com", "secret123");
        String outsiderToken = login("outsider@example.com", "secret123");

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isForbidden());

        var projectResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Task tracker",
                                  "description": "Project management system"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Task tracker"))
                .andExpect(jsonPath("$.owner.email").value("owner@example.com"))
                .andReturn();
        Long projectId = idFrom(projectResult.getResponse().getContentAsByteArray());

        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(projectId));

        mockMvc.perform(get("/api/v1/projects/{projectId}/board", projectId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns.length()").value(5))
                .andExpect(jsonPath("$.columns[0].type").value("BACKLOG"))
                .andExpect(jsonPath("$.columns[4].type").value("DONE"));

        mockMvc.perform(get("/api/v1/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "member@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("member@example.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        var taskResult = mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implement kanban",
                                  "description": "Create task workflow",
                                  "status": "TODO",
                                  "assigneeId": %d,
                                  "deadline": "2026-06-01",
                                  "priority": "HIGH"
                                }
                                """.formatted(((Number) member.get("id")).longValue())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Implement kanban"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assignee.email").value("member@example.com"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andReturn();
        Long taskId = idFrom(taskResult.getResponse().getContentAsByteArray());

        mockMvc.perform(patch("/api/v1/projects/{projectId}/tasks/{taskId}/status", projectId, taskId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/v1/projects/{projectId}/tasks/{taskId}/comments", projectId, taskId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Started working on this"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author.email").value("member@example.com"))
                .andExpect(jsonPath("$.content").value("Started working on this"));

        mockMvc.perform(get("/api/v1/projects/{projectId}/tasks/{taskId}/comments", projectId, taskId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Started working on this"));

        mockMvc.perform(get("/api/v1/projects/{projectId}/board", projectId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[2].type").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.columns[2].tasks[0].id").value(taskId));

        mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid assignee",
                                  "assigneeId": %d
                                }
                                """.formatted(((Number) owner.get("id")).longValue() + 1000)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/projects/{projectId}/board", projectId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void swaggerOpenApiIsAvailableAndDocumentsSecurity() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Projemento API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.paths['/api/v1/projects']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/projects/{projectId}/board']").exists());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void validationAndAccessRulesWork() throws Exception {
        register("validation-owner", "validation-owner@example.com", "secret123");
        register("validation-member", "validation-member@example.com", "secret123");
        Map<String, Object> outsider = register("validation-outsider", "validation-outsider@example.com", "secret123");

        String ownerToken = login("validation-owner@example.com", "secret123");
        String memberToken = login("validation-member@example.com", "secret123");

        mockMvc.perform(post("/api/v1/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "duplicate",
                                  "email": "validation-owner@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());

        var projectResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Validation project"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Long projectId = idFrom(projectResult.getResponse().getContentAsByteArray());

        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "validation-member@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "validation-outsider@example.com"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Task with outsider",
                                  "assigneeId": %d
                                }
                                """.formatted(((Number) outsider.get("id")).longValue())))
                .andExpect(status().isBadRequest());

        var taskResult = mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Task for comments"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Long taskId = idFrom(taskResult.getResponse().getContentAsByteArray());

        mockMvc.perform(post("/api/v1/projects/{projectId}/tasks/{taskId}/comments", projectId, taskId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.content").exists());
    }

    private Map<String, Object> register(String username, String email, String password) throws Exception {
        var result = mockMvc.perform(post("/api/v1/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<Map<String, Object>>() {
                }
        );
    }

    private String login(String email, String password) throws Exception {
        var result = mockMvc.perform(post("/api/users/login/token")
                        .with(httpBasic(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<Map<String, String>>() {
                }
        ).get("accessToken");
    }

    private Long idFrom(byte[] content) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                content,
                new TypeReference<Map<String, Object>>() {
                }
        );
        return ((Number) body.get("id")).longValue();
    }
}
