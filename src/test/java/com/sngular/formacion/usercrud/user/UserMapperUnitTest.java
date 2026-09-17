package com.sngular.formacion.usercrud.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.sngular.formacion.usercrud.user.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;

class UserMapperUnitTest {

    private final UserMapper mapper = new UserMapper();

    /**
     * Documenta el bug conocido: UserMapper.toEntity omite la asignación de email.
     * Ver BUG.md. Cuando se aplique el fix, este test debe reescribirse para afirmar
     * que el email queda asignado (assertThat(entity.getEmail()).isEqualTo(request.email())).
     */
    @Test
    void toEntity_currentlyOmitsEmail_bugKnown() {
        CreateUserRequest request = new CreateUserRequest("Ana Torres", "ana.torres@example.com");

        User entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Ana Torres");
        assertThat(entity.getEmail()).isNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
    }
}
