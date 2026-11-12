package dev.brice.parking.core.port;

import dev.brice.parking.core.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void saveUser(User user);
    Optional<User> findUserByUsername(String username);
    List<User> findAllUsers();

}
