package dev.brice.parking.core.service;

import dev.brice.parking.adapter.repository.inmemory.InMemoryUserRepository;
import dev.brice.parking.adapter.security.BCryptPasswordHasher;
import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.dto.UserUpdateDTO;
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

    @Test
    void updatesRoleAndKeepsPasswordWhenBlank() {
        service.createUser(new UserDTO("admin", "secret1", UserRole.ADMIN));
        User operator = service.createUser(new UserDTO("oper", "secret1", UserRole.OPERATOR));

        User updated = service.updateUser(operator.id(), new UserUpdateDTO(UserRole.ADMIN, ""));

        assertEquals(UserRole.ADMIN, updated.role());
        assertTrue(service.authenticate("oper", "secret1").isPresent());
    }

    @Test
    void updatesPasswordWithNewHash() {
        User operator = service.createUser(new UserDTO("oper", "secret1", UserRole.OPERATOR));

        service.updateUser(operator.id(), new UserUpdateDTO(UserRole.OPERATOR, "newsecret"));

        assertTrue(service.authenticate("oper", "newsecret").isPresent());
        assertTrue(service.authenticate("oper", "secret1").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.updateUser(operator.id(), new UserUpdateDTO(UserRole.OPERATOR, "123")));
    }

    @Test
    void lastAdminCannotBeDemotedNorDeleted() {
        User admin = service.createUser(new UserDTO("admin", "secret1", UserRole.ADMIN));
        User second = service.createUser(new UserDTO("admin2", "secret1", UserRole.ADMIN));

        service.updateUser(second.id(), new UserUpdateDTO(UserRole.OPERATOR, null)); // queda 1 admin
        assertThrows(IllegalArgumentException.class, () -> service.updateUser(admin.id(), new UserUpdateDTO(UserRole.OPERATOR, null)));
        assertThrows(IllegalArgumentException.class, () -> service.deleteUser(admin.id(), "otro"));
    }

    @Test
    void deletesUserButNotOwnAccount() {
        User admin = service.createUser(new UserDTO("admin", "secret1", UserRole.ADMIN));
        User operator = service.createUser(new UserDTO("oper", "secret1", UserRole.OPERATOR));

        assertThrows(IllegalArgumentException.class, () -> service.deleteUser(operator.id(), "OPER"));
        service.deleteUser(operator.id(), admin.username());

        assertTrue(service.findUserByUsername("oper").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteUser(operator.id(), "admin"));
    }
}
