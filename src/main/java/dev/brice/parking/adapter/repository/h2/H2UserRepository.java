package dev.brice.parking.adapter.repository.h2;

import dev.brice.parking.adapter.repository.h2.dao.UserDAO;
import dev.brice.parking.core.entity.User;
import dev.brice.parking.core.port.UserRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class H2UserRepository implements UserRepository {

    private final Jdbi jdbi;
    private final UserDAO userDAO;

    public H2UserRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
        this.userDAO = jdbi.onDemand(UserDAO.class);

        setupTables();
    }

    @Override
    public void saveUser(User user) {
        userDAO.upsertUser(user.id(), user.username(), user.passwordHash(), user.role());
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        return userDAO.findUserById(id);
    }

    @Override
    public void deleteUserById(UUID id) {
        userDAO.deleteUserById(id);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return username == null ? Optional.empty() : userDAO.findUserByUsername(username.trim());
    }

    @Override
    public List<User> findAllUsers() {
        return userDAO.findAllUsers();
    }

    private void setupTables() {
        // "user" es palabra reservada en H2, por eso la tabla se llama app_user
        jdbi.useHandle(handle -> handle.execute("""
                create table if not exists app_user (
                    user_id uuid primary key,
                    username varchar(30) not null unique,
                    password_hash varchar(100) not null,
                    role varchar(16) not null
                )"""));
    }
}
