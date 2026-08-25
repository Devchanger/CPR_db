package com.cpr_db.cpr_db.common;

import java.util.regex.Pattern;

/**
 * D5 password policy: at least 6 characters, letters and digits only.
 * Centralized so DTO constraints and service-level checks cannot drift.
 */
public final class PasswordPolicy {

    public static final String REGEX = "^[0-9A-Za-z]{6,}$";
    public static final String MESSAGE = "password must be at least 6 characters and contain only letters and digits";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }
}
