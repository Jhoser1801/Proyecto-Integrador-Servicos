package dev.brice.parking.adapter.repository.h2.dao;

import dev.brice.parking.core.constant.UserRole;
import dev.brice.parking.core.entity.User;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RegisterRowMapper(UserDAO.Mapper.class)
public interface UserDAO {

    @SqlUpdate("merge into app_user (user_id, username, password_hash, role) key (user_id) values (?,?,?,?)")
    void upsertUser(UUID id, String username, String passwordHash, UserRole role);

    @SqlQuery("select * from app_user where user_id = ?")
    Optional<User> findUserById(UUID id);

    @SqlUpdate("delete from app_user where user_id = ?")
    void deleteUserById(UUID id);

    @SqlQuery("select * from app_user where lower(username) = lower(?)")
    Optional<User> findUserByUsername(String username);

    @SqlQuery("select * from app_user order by username")
    List<User> findAllUsers();

    class Mapper implements RowMapper<User> {
        @Override
        public User map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new User(
                    rs.getObject("user_id", UUID.class),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    UserRole.valueOf(rs.getString("role"))
            );
        }
    }
}
