package com.pos.auth.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A user row.
 * <p>
 * IMPORTANT: this object carries the BCrypt {@code passwordHash} only.
 * A plaintext password is never stored in a field, never logged, and never
 * kept in the session - it lives in a local {@code char[]}/String inside the
 * login flow and is discarded immediately after verification.
 */
public class User {

    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role role;
    private boolean active;
    private boolean mustChangePassword;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(int id, String username, String fullName, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /** BCrypt hash. Never render this in the UI. */
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /** Convenience for other modules: "ADMIN" / "SALES". */
    public String getRoleName() {
        return role == null ? null : role.getName();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Returns a copy with the hash stripped - used for the session object. */
    public User withoutPasswordHash() {
        User copy = new User();
        copy.id = this.id;
        copy.username = this.username;
        copy.fullName = this.fullName;
        copy.role = this.role;
        copy.active = this.active;
        copy.mustChangePassword = this.mustChangePassword;
        copy.lastLoginAt = this.lastLoginAt;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        copy.passwordHash = null;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id == ((User) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Deliberately excludes the password hash. */
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role
                + ", active=" + active + "}";
    }
}
