package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Notification.Notification;
import com.example.berotravel20.data.model.User.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApiService {
    @GET("/api/users/profile")
    Call<User> getProfile();

    @PUT("/api/users/profile")
    Call<User> updateProfile(@Body User user);

    // Notification
    @GET("/api/notifications")
    Call<List<Notification>> getNotifications();

    // Favorites
    @POST("/api/favorites/{placeId}")
    Call<Void> toggleFavorite(@Path("placeId") String placeId);

    @GET("/api/favorites")
    Call<List<String>> getFavorites();
}