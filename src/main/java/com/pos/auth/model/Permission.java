package com.pos.auth.model;

import java.util.Objects;

/**
 * A single permission code, e.g. {@code SALE_CREATE}.
 * Codes are listed in {@link com.pos.auth.security.Permissions}.
 */
public class Permission {

    private final int id;
    private final String code;
    private final String description;

    public Permission(int id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Permission)) {
            return false;
        }
        return Objects.equals(code, ((Permission) o).code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
