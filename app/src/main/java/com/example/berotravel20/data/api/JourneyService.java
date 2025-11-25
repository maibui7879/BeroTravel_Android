package com.example.berotravel20.data.api;

import com.example.berotravel20.data.models.Journey.Journey;
import com.example.berotravel20.data.models.Journey.JourneyRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface JourneyService {

    @GET("api/journeys")
    Call<List<Journey>> getMyJourneys();

    @POST("api/journeys")
    Call<Journey> createJourney(@Body JourneyRequest request);

    @GET("api/journeys/{journeyId}")
    Call<Journey> getJourneyDetail(@Path("journeyId") String journeyId);

    @PUT("api/journeys/{journeyId}")
    Call<Journey> updateJourney(
            @Path("journeyId") String journeyId,
            @Body JourneyRequest request
    );

    @DELETE("api/journeys/{journeyId}")
    Call<Void> deleteJourney(@Path("journeyId") String journeyId);

    @PUT("api/journeys/{journeyId}/status")
    Call<Journey> updateJourneyStatus(@Path("journeyId") String journeyId);

    @POST("api/journeys/{journeyId}/places/{placeId}/visit")
    Call<Journey> markPlaceVisited(
            @Path("journeyId") String journeyId,
            @Path("placeId") String placeId
    );
}
