package dev.brice.parking.core.service;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.dto.UserUpdateDTO;
import dev.brice.parking.core.entity.User;
import dev.brice.parking.core.port.PasswordHasher;
import dev.brice.parking.core.port.UserRepository;

import java.util.List;
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
        if (request.password() == null) {
            throw new IllegalArgumentException("La contrasena debe tener minimo " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        User user = request.toEntity(UUID.randomUUID(), passwordHasher.hash(validPassword(request.password())));
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

    public List<User> findAllUsers() {
        return repository.findAllUsers();
    }

    public Optional<User> findUserById(UUID id) {
        return repository.findUserById(id);
    }

    // Cambia el rol y, si viene una contrasena nueva, la guarda con hash. No se puede dejar el sistema sin administradores
    public User updateUser(UUID id, UserUpdateDTO request) {
        User current = findExistingUser(id);
        if (request.role() == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
        if (current.role() == UserRole.ADMIN && request.role() != UserRole.ADMIN && countAdmins() <= 1) {
            throw new IllegalArgumentException("Debe existir al menos un administrador");
        }
        User updated = current.withRole(request.role());
        if (request.password() != null && !request.password().isBlank()) {
            updated = updated.withPasswordHash(passwordHasher.hash(validPassword(request.password())));
        }
        repository.saveUser(updated);
        return updated;
    }

    // nadie puede eliminar su propia cuenta
    public void deleteUser(UUID id, String actingUsername) {
        User user = findExistingUser(id);
        if (user.username().equalsIgnoreCase(actingUsername)) {
            throw new IllegalArgumentException("No puede eliminar su propia cuenta");
        }
        if (user.role() == UserRole.ADMIN && countAdmins() <= 1) {
            throw new IllegalArgumentException("Debe existir al menos un administrador");
        }
        repository.deleteUserById(id);
    }

    private long countAdmins() {
        return repository.findAllUsers().stream().filter(user -> user.role() == UserRole.ADMIN).count();
    }

    private User findExistingUser(UUID id) {
        return repository.findUserById(id).orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
    }

    private String validPassword(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("La contrasena debe tener minimo " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        return password;
    }
}
