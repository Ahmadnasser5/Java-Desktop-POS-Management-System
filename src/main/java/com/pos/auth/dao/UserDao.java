package com.pos.auth.dao;

import com.pos.auth.model.User;

import java.util.List;
import java.util.Optional;

/**
 * All SQL touching {@code users} lives behind this interface.
 * Services and controllers never see JDBC types.
 */
public interface UserDao {

    Optional<User> findByUsername(String username);

    Optional<User> findById(int id);

    List<User> findAll();

    /** Case-insensitive match on username or full name. */
    List<User> search(String term);

    /**
     * @param passwordHash an already hashed password - this method never hashes
     * @return the generated user id
     */
    int insert(User user, String passwordHash);

    /** Updates username, full name, role and active flag. Not the password. */
    void update(User user);

    void updateActive(int userId, boolean active);

    void updatePasswordHash(int userId, String passwordHash, boolean mustChangePassword);

    void updateLastLogin(int userId);

    boolean existsByUsername(String username);

    boolean existsByUsernameExcludingId(String username, int userId);

    /** Hard delete. Only allowed when the user owns no historical records. */
    void deleteById(int userId);

    /** Guard against deleting or demoting the last administrator. */
    int countActiveAdmins();
}
