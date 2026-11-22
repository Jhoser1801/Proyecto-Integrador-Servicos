package dev.brice.parking.core.dto;

import dev.brice.parking.core.constant.UserRole;

// Datos que se pueden cambiar de un usuario. Si password viene vacia, se conserva la contrasena actual
public record UserUpdateDTO(UserRole role, String password) {
}
