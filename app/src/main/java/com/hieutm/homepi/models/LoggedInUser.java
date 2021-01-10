package com.hieutm.homepi.models;

/**
 * Data class that captures user information for logged in users
 */
public class LoggedInUser {
    private final String userId;
    private final String displayName;
    private final String commandTopic;
    private final String statusTopic;

    public LoggedInUser(String userId, String displayName, String commandTopic, String statusTopic) {
        this.userId = userId;
        this.displayName = displayName;
        this.commandTopic = commandTopic;
        this.statusTopic = statusTopic;
    }

    public String getUsername() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCommandTopic() {
        return commandTopic;
    }

    public String getStatusTopic() {
        return statusTopic;
    }
}