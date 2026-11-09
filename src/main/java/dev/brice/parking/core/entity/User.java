package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.UserRole;

import java.util.UUID;
import java.util.regex.Pattern;

public record User(
        UUID id,
        String username,
        String passwordHash,
        UserRole role
) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,30}$");

    public User {
        if (id == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 30 caracteres (letras, numeros, . _ -)");
        }
        username = username.trim();
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
    }
}
