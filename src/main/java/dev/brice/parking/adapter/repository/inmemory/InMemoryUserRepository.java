package dev.brice.parking.adapter.repository.inmemory;

import dev.brice.parking.core.entity.User;
import dev.brice.parking.core.port.out.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public void saveUser(User user) {
        users.put(user.username().toLowerCase(), user);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return username == null ? Optional.empty() : Optional.ofNullable(users.get(username.trim().toLowerCase()));
    }

    @Override
    public List<User> findAllUsers() {
        return List.copyOf(users.values());
    }
}
