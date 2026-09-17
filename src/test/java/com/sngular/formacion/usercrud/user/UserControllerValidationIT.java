package com.sngular.formacion.usercrud.user;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class UserControllerValidationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    private Long seedId;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        seedId = repository.save(new User(null, "Seed", "seed@example.com", Instant.now())).getId();
    }

    @Test
    void createUser_withBlankName_returns400_withProblemDetail() throws Exception {
        String payload = """
                {"name":"","email":"ana@example.com"}
                """;
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")));
    }

    @Test
    void createUser_withInvalidEmail_returns400_withProblemDetail() throws Exception {
        String payload = """
                {"name":"Ana","email":"no-es-un-email"}
                """;
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("email")));
    }

    @Test
    void createUser_withNameOver100Chars_returns400() throws Exception {
        String longName = "a".repeat(101);
        String payload = """
                {"name":"%s","email":"ana@example.com"}
                """.formatted(longName);
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("name")));
    }

    @Test
    void updateUser_withBlankEmail_returns400() throws Exception {
        String payload = """
                {"name":"Ana","email":""}
                """;
        mockMvc.perform(put("/users/{id}", seedId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("email")));
    }
}
