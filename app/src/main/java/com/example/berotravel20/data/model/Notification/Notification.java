package com.example.berotravel20.data.model.Notification;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Notification implements Serializable {
    @SerializedName("_id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("message")
    private String message;

    @SerializedName("read")
    private boolean read;

    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}