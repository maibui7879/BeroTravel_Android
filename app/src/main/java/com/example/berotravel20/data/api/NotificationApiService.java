package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Notification.Notification;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationApiService {
    // Lấy danh sách thông báo
    @GET("/api/notifications")
    Call<List<Notification>> getNotifications();

    // Cập nhật trạng thái đã đọc
    @PUT("/api/notifications/{notification_id}")
    Call<Notification> markAsRead(@Path("notification_id") String notificationId);
}