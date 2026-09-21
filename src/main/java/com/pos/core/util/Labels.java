package com.pos.core.util;

import com.pos.auth.model.Role;

/**
 * Arabic display text for values whose underlying code must stay in English
 * (role names used by {@link com.pos.auth.service.AuthorizationService},
 * status booleans...).
 * <p>
 * Never change {@code roles.name} in the database to Arabic - permission
 * checks compare against the English constants in {@link Role}. This class
 * is the only translation layer.
 */
public final class Labels {

    private Labels() {
    }

    /** @return the Arabic label for a role name such as "ADMIN" or "SALES" */
    public static String roleName(String roleCode) {
        if (roleCode == null) {
            return "-";
        }
        return switch (roleCode.toUpperCase(java.util.Locale.ROOT)) {
            case Role.ADMIN -> "مدير النظام";
            case Role.SALES -> "كاشير";
            default -> roleCode;
        };
    }

    public static String activeStatus(boolean active) {
        return active ? "نشط" : "غير نشط";
    }
}
