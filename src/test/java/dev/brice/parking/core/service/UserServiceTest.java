package dev.brice.parking.core.service;

import dev.brice.parking.adapter.repository.inmemory.InMemoryUserRepository;
import dev.brice.parking.adapter.security.BCryptPasswordHasher;
import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new InMemoryUserRepository(), new BCryptPasswordHasher());
    }

    @Test
    void storesHashedPasswordAndAuthenticates() {
        User user = service.createUser(new UserDTO("admin", "secret1", UserRole.ADMIN));

        assertNotEquals("secret1", user.passwordHash());
        assertTrue(service.authenticate("admin", "secret1").isPresent());
        assertTrue(service.authenticate("admin", "wrong").isEmpty());
        assertTrue(service.authenticate("nobody", "secret1").isEmpty());
    }

    @Test
    void rejectsDuplicateUserAndShortPassword() {
        service.createUser(new UserDTO("admin", "secret1", UserRole.ADMIN));
        assertThrows(IllegalArgumentException.class, () -> service.createUser(new UserDTO("ADMIN", "secret1", UserRole.ADMIN)));
        assertThrows(IllegalArgumentException.class, () -> service.createUser(new UserDTO("other", "123", UserRole.OPERATOR)));
    }
}
