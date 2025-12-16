package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Journey.Journey;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface JourneyApiService {
    @GET("/api/journeys")
    Call<List<Journey>> getJourneys();

    @POST("/api/journeys")
    Call<Void> createJourney(@Body Journey.Request request);
}
