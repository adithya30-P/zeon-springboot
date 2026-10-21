package com.zeonskills.config;

/**
 * Session attribute names, matching the keys the original Flask app stored
 * in its session dict: username, email, role, user_id.
 */
public final class SessionKeys {
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String ROLE = "role";
    public static final String USER_ID = "userId";

    private SessionKeys() {}
}
