package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.PlaceStatus.PlaceStatus;
import com.example.berotravel20.data.model.PlaceStatus.PlaceStatusRequest;


import retrofit2.Call;
import retrofit2.http.*;

public interface PlaceStatusApiService {

    // --- GROUP 1: Thao tác theo Place ID ---

    // GET /api/place-status/place/{placeId}
    @GET("/api/place-status/place/{placeId}")
    Call<PlaceStatus> getStatusByPlaceId(@Path("placeId") String placeId);

    // PUT /api/place-status/place/{placeId}
    @PUT("/api/place-status/place/{placeId}")
    Call<PlaceStatus> updateStatusByPlaceId(@Path("placeId") String placeId, @Body PlaceStatusRequest request);

    // DELETE /api/place-status/place/{placeId}
    @DELETE("/api/place-status/place/{placeId}")
    Call<Void> deleteStatusByPlaceId(@Path("placeId") String placeId);


    // --- GROUP 2: Thao tác theo Status ID (ID của bảng status) ---

    // GET /api/place-status/{id}
    @GET("/api/place-status/{id}")
    Call<PlaceStatus> getStatusById(@Path("id") String id);

    // PUT /api/place-status/{id}
    @PUT("/api/place-status/{id}")
    Call<PlaceStatus> updateStatusById(@Path("id") String id, @Body PlaceStatusRequest request);

    // DELETE /api/place-status/{id}
    @DELETE("/api/place-status/{id}")
    Call<Void> deleteStatusById(@Path("id") String id);


    // --- GROUP 3: Tạo mới ---

    // POST /api/place-status
    @POST("/api/place-status")
    Call<PlaceStatus> createPlaceStatus(@Body PlaceStatusRequest request);
}
