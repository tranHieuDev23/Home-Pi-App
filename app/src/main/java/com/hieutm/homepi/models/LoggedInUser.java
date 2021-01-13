package com.hieutm.homepi.models;

/**
 * Data class that captures user information for logged in users
 */
public class LoggedInUser {
    private final String userId;
    private final String displayName;

    public LoggedInUser(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUsername() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}