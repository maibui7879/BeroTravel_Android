package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Notification.Notification;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.User.User;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApiService {
    @GET("/api/users/profile")
    Call<User> getProfile();

    @PUT("/api/users/profile")
    Call<User> updateProfile(@Body User user);

    // Thêm hàm này để upload ảnh + thông tin
    @Multipart
    @PUT("/api/users/profile")
    Call<User> updateProfileMultipart(
            @Part("name") RequestBody name,
            @Part("bio") RequestBody bio,
            @Part MultipartBody.Part avatar, // File ảnh đại diện
            @Part MultipartBody.Part cover   // File ảnh bìa
    );
    @PUT("/api/users/profile")
    Call<User> changePassword(@Body User userWithNewPassword);

    // Notification
    @GET("/api/notifications")
    Call<List<Notification>> getNotifications();

    // Favorites
    @POST("/api/favorites/{placeId}")
    Call<Void> toggleFavorite(@Path("placeId") String placeId);

    @GET("/api/favorites")
    Call<List<Place>> getFavorites();
}