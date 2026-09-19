package com.pos.auth.model;

import java.util.Objects;

/**
 * A role row. Immutable on purpose: roles are reference data.
 */
public class Role {

    /** Role names that the application relies on. Must match the roles table. */
    public static final String ADMIN = "ADMIN";
    public static final String SALES = "SALES";

    private final int id;
    private final String name;
    private final String description;

    public Role(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAdmin() {
        return ADMIN.equalsIgnoreCase(name);
    }

    public boolean isSales() {
        return SALES.equalsIgnoreCase(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role)) {
            return false;
        }
        return id == ((Role) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
