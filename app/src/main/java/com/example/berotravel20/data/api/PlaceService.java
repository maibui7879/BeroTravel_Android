package com.example.berotravel20.data.api;

import com.example.berotravel20.data.models.Place.Place;
import com.example.berotravel20.data.models.Place.PlaceRequest;
import com.example.berotravel20.data.models.Place.PlaceResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PlaceService {

    // Lấy tổng số địa điểm
    @GET("places/count")
    Call<Map<String, Integer>> getPlaceCount();

    // Lấy tất cả địa điểm
    @GET("places")
    Call<List<Place>> getAllPlaces();

    // Lấy địa điểm theo ID
    @GET("places/{id}")
    Call<Place> getPlaceById(@Path("id") String placeId);

    // Tìm địa điểm gần vị trí hiện tại
    @GET("places/search/nearby")
    Call<List<Place>> getNearbyPlaces(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius") double radius
    );

    // Tạo địa điểm mới
    @POST("places")
    Call<PlaceResponse> createPlace(@Body PlaceRequest request);

    // Cập nhật địa điểm theo ID
    @PUT("places/{id}")
    Call<PlaceResponse> updatePlace(@Path("id") String placeId, @Body PlaceRequest request);

    // Xóa địa điểm
    @DELETE("places/{id}")
    Call<Void> deletePlace(@Path("id") String placeId);

    @PUT("places/images/{id}")
    Call<PlaceResponse> updatePlaceImages(
            @Path("id") String placeId,
            @Body Map<String, Object> images
    );
}

