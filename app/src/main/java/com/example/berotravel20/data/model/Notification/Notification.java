package com.example.berotravel20.data.model.Notification;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("_id")
    public String id;

    @SerializedName("user_id")
    public String userId; // Map chính xác chuỗi ID (vd: "68fb8f3b...")

    public String message;

    public boolean read;

    // JSON trả về 2 trường thời gian giống nhau, bạn map cả 2 hoặc chọn 1 để dùng
    @SerializedName("created_at")
    public String createdAtRaw;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    @SerializedName("__v")
    public int version; // Trường version của MongoDB (thường không cần hiển thị lên UI)
}