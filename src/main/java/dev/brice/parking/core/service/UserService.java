package dev.brice.parking.core.service;

import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.entity.User;
import dev.brice.parking.core.port.PasswordHasher;
import dev.brice.parking.core.port.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class UserService {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    public User createUser(UserDTO request) {
        if (request.password() == null || request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("La contrasena debe tener minimo " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        User user = request.toEntity(UUID.randomUUID(), passwordHasher.hash(request.password()));
        if (repository.findUserByUsername(user.username()).isPresent()) {
            throw new IllegalArgumentException("Ya existe el usuario " + user.username());
        }
        repository.saveUser(user);
        return user;
    }

    public Optional<User> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return repository.findUserByUsername(username.trim())
                .filter(user -> passwordHasher.matches(password, user.passwordHash()));
    }

    public Optional<User> findUserByUsername(String username) {
        return repository.findUserByUsername(username);
    }
}
