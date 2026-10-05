package com.hinal.taskmanagementapi.controller;

import com.hinal.taskmanagementapi.repository.TaskRepository;
import com.hinal.taskmanagementapi.entity.User;
import com.hinal.taskmanagementapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearTasks() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new User("alice", passwordEncoder.encode("password123")));
        userRepository.save(new User("bob", passwordEncoder.encode("password123")));
    }

    @Test
    void performsTaskCrudOperations() throws Exception {
        String createdResponse = mockMvc.perform(post("/api/tasks")
                        .with(httpBasic("alice", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare interview",
                                  "description": "Review Spring Boot",
                                  "dueDate": "2026-11-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Prepare interview"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer taskId = com.jayway.jsonpath.JsonPath.read(createdResponse, "$.id");

        mockMvc.perform(get("/api/tasks").with(httpBasic("alice", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId));

        mockMvc.perform(get("/api/tasks/{id}", taskId).with(httpBasic("alice", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Prepare interview"));

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .with(httpBasic("alice", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare for interviews",
                                  "description": "Review Spring Boot and JPA",
                                  "status": "IN_PROGRESS",
                                  "priority": "HIGH",
                                  "dueDate": "2026-11-05"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Prepare for interviews"))
                .andExpect(jsonPath("$.description").value("Review Spring Boot and JPA"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-11-05"));

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .with(httpBasic("alice", "password123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", taskId).with(httpBasic("alice", "password123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Task with id " + taskId + " was not found"))
                .andExpect(jsonPath("$.path").value("/api/tasks/" + taskId));
    }

    @Test
    void rejectsInvalidCreateRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic("alice", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Review Spring Boot"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/tasks"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not be blank"));
    }

    @Test
    void returnsConsistentErrorForMalformedRequestBody() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic("alice", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare interview",
                                  "dueDate": "not-a-date"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request body is invalid"))
                .andExpect(jsonPath("$.path").value("/api/tasks"));
    }

    @Test
    void rejectsPutWhenRequiredReplacementFieldsAreMissing() throws Exception {
        String createdResponse = mockMvc.perform(post("/api/tasks")
                        .with(httpBasic("alice", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Existing task"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer taskId = com.jayway.jsonpath.JsonPath.read(createdResponse, "$.id");

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .with(httpBasic("alice", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Replacement task"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").value("Status is required"))
                .andExpect(jsonPath("$.fieldErrors.priority").value("Priority is required"));

        mockMvc.perform(get("/api/tasks/{id}", taskId).with(httpBasic("alice", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Existing task"));
    }

    @Test
    void requiresAuthenticationForTaskEndpoints() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void taskOwnerCannotBeAccessedByAnotherUser() throws Exception {
        String createdResponse = mockMvc.perform(post("/api/tasks")
                        .with(httpBasic("alice", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Alice task"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer taskId = com.jayway.jsonpath.JsonPath.read(createdResponse, "$.id");

        mockMvc.perform(get("/api/tasks/{id}", taskId).with(httpBasic("bob", "password123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TASK_NOT_FOUND"));

        mockMvc.perform(get("/api/tasks").with(httpBasic("bob", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void registersUserWithHashedPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "charlie",
                                  "password": "secure-pass-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("charlie"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User registeredUser = userRepository.findByUsername("charlie").orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals(
                "secure-pass-123", registeredUser.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches("secure-pass-123", registeredUser.getPasswordHash()));
    }

    @Test
    void loginAcceptsValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void loginRejectsInvalidCredentialsWithGenericError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void rejectsDuplicateUsernameWithConflict() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "secure-pass-123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("USERNAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }
}
