package com.sngular.formacion.usercrud.user;

import com.sngular.formacion.usercrud.user.dto.CreateUserRequest;
import com.sngular.formacion.usercrud.user.dto.UpdateUserRequest;
import com.sngular.formacion.usercrud.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setName(request.name());
        return user;
    }

    public void applyUpdate(UpdateUserRequest request, User existing) {
        existing.setName(request.name());
        existing.setEmail(request.email());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
