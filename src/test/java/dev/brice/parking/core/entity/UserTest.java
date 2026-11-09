package dev.brice.parking.core.entity;

import dev.brice.parking.core.constant.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void createsValidUser() {
        User user = new User(UUID.randomUUID(), " admin ", "hash", UserRole.ADMIN);
        assertEquals("admin", user.username());
    }

    @Test
    void rejectsInvalidUsername() {
        assertThrows(IllegalArgumentException.class, () -> new User(UUID.randomUUID(), "ab", "hash", UserRole.ADMIN));
        assertThrows(IllegalArgumentException.class, () -> new User(UUID.randomUUID(), "con espacio", "hash", UserRole.ADMIN));
        assertThrows(IllegalArgumentException.class, () -> new User(UUID.randomUUID(), null, "hash", UserRole.ADMIN));
    }

    @Test
    void rejectsMissingHashOrRole() {
        assertThrows(IllegalArgumentException.class, () -> new User(UUID.randomUUID(), "admin", " ", UserRole.ADMIN));
        assertThrows(IllegalArgumentException.class, () -> new User(UUID.randomUUID(), "admin", "hash", null));
    }
}
