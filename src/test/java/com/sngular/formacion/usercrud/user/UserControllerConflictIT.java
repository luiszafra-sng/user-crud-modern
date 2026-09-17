package com.sngular.formacion.usercrud.user;

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
class UserControllerConflictIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    private Long secondId;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(new User(null, "First", "first@example.com", Instant.now()));
        secondId = repository.save(new User(null, "Second", "second@example.com", Instant.now())).getId();
    }

    @Test
    void updateUser_toEmailAlreadyTakenByAnotherUser_returns409() throws Exception {
        String payload = """
                {"name":"Second","email":"first@example.com"}
                """;
        mockMvc.perform(put("/users/{id}", secondId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Email Already Exists"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }
}
