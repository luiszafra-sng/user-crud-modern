package com.sngular.formacion.usercrud.user;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerHappyPathIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    private Long seedId;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        User seeded = repository.save(new User(null, "Seed", "seed@example.com", Instant.parse("2026-09-16T09:00:00Z")));
        seedId = seeded.getId();
    }

    @Test
    void listUsers_returnsSeeded() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("seed@example.com"));
    }

    @Test
    void getUser_byExistingId_returnsUser() throws Exception {
        mockMvc.perform(get("/users/{id}", seedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(seedId))
                .andExpect(jsonPath("$.name").value("Seed"))
                .andExpect(jsonPath("$.email").value("seed@example.com"));
    }

    @Test
    void getUser_byMissingId_returns404_withProblemDetail() throws Exception {
        mockMvc.perform(get("/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("User Not Found"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void updateUser_byExistingId_returnsUpdated() throws Exception {
        String payload = """
                {"name":"Renombrado","email":"seed@example.com"}
                """;
        mockMvc.perform(put("/users/{id}", seedId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renombrado"))
                .andExpect(jsonPath("$.email").value("seed@example.com"));
    }

    @Test
    void updateUser_byMissingId_returns404() throws Exception {
        String payload = """
                {"name":"Nadie","email":"nadie@example.com"}
                """;
        mockMvc.perform(put("/users/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void deleteUser_byExistingId_returns204() throws Exception {
        mockMvc.perform(delete("/users/{id}", seedId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_byMissingId_returns404() throws Exception {
        mockMvc.perform(delete("/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
