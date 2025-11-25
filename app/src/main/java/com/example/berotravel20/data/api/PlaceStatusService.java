package com.example.berotravel20.data.api;

import com.example.berotravel20.data.models.PlaceStatus.PlaceStatus;
import com.example.berotravel20.data.models.PlaceStatus.PlaceStatusRequest;
import com.example.berotravel20.data.models.PlaceStatus.PlaceStatusResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface PlaceStatusService {

    // GET /api/place-status/{placeId}
    @GET("/api/place-status/{placeId}")
    Call<PlaceStatus> getStatusByPlaceId(@Path("placeId") String placeId);

    // POST /api/place-status
    @POST("/api/place-status")
    Call<PlaceStatusResponse> createPlaceStatus(@Body PlaceStatusRequest request);

    // PUT /api/place-status/{id}
    @PUT("/api/place-status/{id}")
    Call<PlaceStatusResponse> updatePlaceStatus(
            @Path("id") String id,
            @Body PlaceStatusRequest request
    );

    // DELETE /api/place-status/{id}
    @DELETE("/api/place-status/{id}")
    Call<Void> deletePlaceStatus(@Path("id") String id);
}
