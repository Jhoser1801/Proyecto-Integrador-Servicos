package dev.brice.parking.adapter.security;

import dev.brice.parking.core.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Implementa el puerto con el BCrypt de Spring Security
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
