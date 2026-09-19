package com.pos.auth.security;

/**
 * Permission codes shared across the whole project.
 * <p>
 * These constants mirror the {@code permissions} table (see
 * {@code 02_seed_data.sql}). Other engineers should reference these constants
 * instead of typing the string literal, so a rename is a compile error rather
 * than a silent security hole.
 */
public final class Permissions {

    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";

    public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
    public static final String PRODUCT_MANAGE = "PRODUCT_MANAGE";

    public static final String SALE_CREATE = "SALE_CREATE";
    public static final String SALE_CANCEL = "SALE_CANCEL";

    public static final String PAYMENT_PROCESS = "PAYMENT_PROCESS";

    public static final String REPORT_VIEW = "REPORT_VIEW";
    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";

    private Permissions() {
    }
}
