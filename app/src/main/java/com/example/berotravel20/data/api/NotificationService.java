package com.example.berotravel20.data.api;

import com.example.berotravel20.data.models.Notification.Notification;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationService {

    // GET /notifications
    @GET("/notifications")
    Call<List<Notification>> getNotifications();

    // PUT /notifications/{id}
    @PUT("/notifications/{id}")
    Call<Notification> markAsRead(@Path("id") String notificationId);
}
