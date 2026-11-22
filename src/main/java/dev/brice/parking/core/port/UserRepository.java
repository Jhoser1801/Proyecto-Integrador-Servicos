package dev.brice.parking.core.port;

import dev.brice.parking.core.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void saveUser(User user);
    Optional<User> findUserById(UUID id);
    Optional<User> findUserByUsername(String username);
    List<User> findAllUsers();
    void deleteUserById(UUID id);

}
