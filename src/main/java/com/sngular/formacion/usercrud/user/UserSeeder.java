package com.sngular.formacion.usercrud.user;

import java.time.Clock;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {

    private final UserRepository repository;
    private final Clock clock;

    public UserSeeder(UserRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    // Se salta el mapper (bloqueado por el bug) para permitir probar GET/PUT/DELETE
    // en local sin depender de POST /users. Ver BUG.md.
    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new User(null, "Seed User", "seed@example.com", clock.instant()));
        repository.save(new User(null, "Second Seed", "second@example.com", clock.instant()));
    }
}
