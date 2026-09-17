package com.sngular.formacion.usercrud.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerCreateBugIT {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Documenta el bug conocido: POST /users lanza NullPointerException dentro de
     * UserService.normalizeEmail porque UserMapper.toEntity omite setEmail(...).
     * MockMvc envuelve la excepción en jakarta.servlet.ServletException y la propaga
     * al llamante (no llega a andExpect / andReturn), por lo que la aserción va sobre
     * el propio invocación de perform(...).
     *
     * Ver BUG.md. Cuando se aplique el fix, reescribir a:
     *     mockMvc.perform(post("/users")...).andExpect(status().isCreated())
     */
    @Test
    void createUser_currentlyThrowsNPE_bugKnown() {
        String payload = """
                {"name":"Ana Torres","email":"ana.torres@example.com"}
                """;

        assertThatThrownBy(() -> mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)))
                .as("La creación debe propagar una excepción con NullPointerException como causa raíz")
                .hasRootCauseInstanceOf(NullPointerException.class);
    }
}
