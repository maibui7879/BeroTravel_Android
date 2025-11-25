package com.example.berotravel20.data.repository;

import android.content.Context;

import com.example.berotravel20.data.api.NotificationService;
import com.example.berotravel20.data.api.RetrofitClient;
import com.example.berotravel20.data.local.SessionManager;
import com.example.berotravel20.data.models.Notification.Notification;

import java.util.List;

import retrofit2.Call;

public class NotificationRepository {

    private final NotificationService service;

    public NotificationRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        service = RetrofitClient.getClient(sessionManager).create(NotificationService.class);
    }

    public Call<List<Notification>> getNotifications() {
        return service.getNotifications();
    }

    public Call<Notification> markAsRead(String notificationId) {
        return service.markAsRead(notificationId);
    }
}
