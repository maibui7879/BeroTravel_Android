package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceResponse; // Import model mới
import com.example.berotravel20.data.model.Place.PlaceImagesRequest;

import retrofit2.Call;
import retrofit2.http.*;

public interface PlaceApiService {

    // 1. Lấy tất cả (GET /places) -> Trả về PlaceResponse
    @GET("/api/places")
    Call<PlaceResponse> getAllPlaces();

    // 2. Tìm kiếm (GET /api/places?...) -> Trả về PlaceResponse
    @GET("/api/places/search/nearby")
    Call<PlaceResponse> searchNearby(
            @Query("latitude") double lat,
            @Query("longitude") double lng,
            @Query("radius") Integer radius,
            @Query("name") String name, // Keyword tìm theo tên
            @Query("category") String category
    );

    @GET("/api/places/{id}")
    Call<Place> getPlace(@Path("id") String id);

    // ... Các API POST/PUT/DELETE giữ nguyên (vì thường backend trả về Object đã tạo hoặc Void)
    @POST("/api/places")
    Call<Place> createPlace(@Body Place.Request request);

    @PUT("/api/places/{id}")
    Call<Place> updatePlace(@Path("id") String id, @Body Place.Request request);

    @DELETE("/api/places/{id}")
    Call<Void> deletePlace(@Path("id") String id);

    @PUT("/api/places/images/{id}")
    Call<Void> updatePlaceImages(@Path("id") String id, @Body PlaceImagesRequest request);
}