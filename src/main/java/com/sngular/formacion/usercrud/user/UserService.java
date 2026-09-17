package com.sngular.formacion.usercrud.user;

import com.sngular.formacion.usercrud.error.UserNotFoundException;
import com.sngular.formacion.usercrud.user.dto.CreateUserRequest;
import com.sngular.formacion.usercrud.user.dto.UpdateUserRequest;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final Clock clock;

    public UserService(UserRepository repository, UserMapper mapper, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    public User create(CreateUserRequest request) {
        User user = mapper.toEntity(request);
        normalizeEmail(user);
        user.setCreatedAt(clock.instant());
        return repository.save(user);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User update(Long id, UpdateUserRequest request) {
        User existing = findById(id);
        mapper.applyUpdate(request, existing);
        normalizeEmail(existing);
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private void normalizeEmail(User user) {
        user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
    }
}
