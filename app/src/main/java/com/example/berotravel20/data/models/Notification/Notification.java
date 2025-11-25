package com.example.berotravel20.data.models.Notification;

public class Notification {
    private String _id;
    private String message;
    private boolean read;
    private String createdAt;
    private String updatedAt;

    public String get_id() {
        return _id;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}

