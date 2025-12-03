package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceCountResponse;
import com.example.berotravel20.data.model.Place.PlaceImagesRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface PlaceApiService {

    // 1. GET /places/count - Lấy tổng số địa điểm
    @GET("/api/places/count")
    Call<PlaceCountResponse> getPlacesCount();

    // 2. GET /places - Lấy danh sách tất cả
    @GET("/api/places")
    Call<List<Place>> getAllPlaces();

    // 3. POST /places - Tạo địa điểm mới
    @POST("/api/places")
    Call<Place> createPlace(@Body Place.Request request);

    // 4. GET /places/{id} - Lấy chi tiết theo ID
    @GET("/api/places/{id}")
    Call<Place> getPlace(@Path("id") String id);

    // 5. PUT /places/{id} - Cập nhật thông tin
    @PUT("/api/places/{id}")
    Call<Place> updatePlace(@Path("id") String id, @Body Place.Request request);

    // 6. DELETE /places/{id} - Xóa địa điểm
    @DELETE("/api/places/{id}")
    Call<Void> deletePlace(@Path("id") String id);

    // 7. PUT /places/images/{id} - Cập nhật ảnh
    @PUT("/api/places/images/{id}")
    Call<Void> updatePlaceImages(@Path("id") String id, @Body PlaceImagesRequest request);

    // 8. GET /places/search/nearby - Tìm kiếm gần đây
    @GET("/api/places/search/nearby")
    Call<List<Place>> searchNearby(
            @Query("latitude") double lat,
            @Query("longitude") double lng,
            @Query("radius") Integer radius,
            @Query("name") String name,
            @Query("category") String category
    );
}