package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Journey.Journey;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface JourneyApiService {
    @GET("/api/journeys")
    Call<com.example.berotravel20.models.JourneyResponse> getJourneys();

    @POST("/api/journeys")
    Call<com.google.gson.JsonObject> createJourney(@Body com.example.berotravel20.models.CreateJourneyRequest request);

    @PUT("/api/journeys/add-place")
    Call<com.google.gson.JsonObject> addPlaceToJourney(@Body com.example.berotravel20.models.AddPlaceRequest request);

    @DELETE("/api/journeys/{journeyId}")
    Call<com.google.gson.JsonObject> deleteJourney(@Path("journeyId") String journeyId);

    @GET("/api/journeys/{journeyId}")
    Call<com.example.berotravel20.models.SingleJourneyResponse> getJourneyById(@Path("journeyId") String journeyId);

    @DELETE("/api/journeys/{journeyId}/places/{placeId}")
    Call<com.google.gson.JsonObject> removePlaceFromJourney(@Path("journeyId") String journeyId,
            @Path("placeId") String placeId);
}
