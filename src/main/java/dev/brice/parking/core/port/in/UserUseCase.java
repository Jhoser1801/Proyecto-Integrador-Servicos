package dev.brice.parking.core.port.in;

import dev.brice.parking.core.dto.UserDTO;
import dev.brice.parking.core.entity.User;

import java.util.Optional;

public interface UserUseCase {

    User createUser(UserDTO request);
    Optional<User> authenticate(String username, String password);
    Optional<User> findUserByUsername(String username);

}
