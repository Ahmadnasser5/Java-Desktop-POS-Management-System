package com.pos.core.util;

import java.util.regex.Pattern;

/**
 * Small, dependency-free input rules shared by the auth forms.
 */
public final class ValidationUtil {

    /** letters, digits, dot, underscore, dash - 3 to 50 characters */
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    public static final int MIN_PASSWORD_LENGTH = 8;

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    /**
     * Password policy: at least 8 characters, containing a letter and a digit.
     * Deliberately modest - a strict policy on a shop floor terminal only
     * produces passwords written on sticky notes.
     */
    public static boolean isStrongEnough(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    public static String passwordPolicyMessage() {
        return "يجب ألا تقل كلمة المرور عن " + MIN_PASSWORD_LENGTH
                + " أحرف، وتحتوي على حرف واحد ورقم واحد على الأقل.";
    }

    /** Overwrites a password array so it does not linger in memory. */
    public static void wipe(char[] password) {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
        }
    }
}
