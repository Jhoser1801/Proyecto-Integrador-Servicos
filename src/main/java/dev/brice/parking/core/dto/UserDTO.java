package dev.brice.parking.core.dto;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.entity.User;

import java.util.UUID;

// Trae la contrasena en texto plano; el service la convierte en hash antes de crear la entidad
public record UserDTO(String username, String password, UserRole role) {
    public User toEntity(UUID id, String passwordHash) {
        return new User(id, username, passwordHash, role);
    }
}
