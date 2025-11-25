package com.example.berotravel20.data.repository;

import android.content.Context;

import com.example.berotravel20.data.api.JourneyService;
import com.example.berotravel20.data.api.RetrofitClient;
import com.example.berotravel20.data.local.SessionManager;
import com.example.berotravel20.data.models.Journey.Journey;
import com.example.berotravel20.data.models.Journey.JourneyRequest;

import java.util.List;

import retrofit2.Call;

public class JourneyRepository {

    private final JourneyService service;

    public JourneyRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        service = RetrofitClient.getClient(sessionManager).create(JourneyService.class);
    }

    public Call<List<Journey>> getMyJourneys() {
        return service.getMyJourneys();
    }

    public Call<Journey> createJourney(JourneyRequest request) {
        return service.createJourney(request);
    }

    public Call<Journey> getJourneyDetail(String journeyId) {
        return service.getJourneyDetail(journeyId);
    }

    public Call<Journey> updateJourney(String journeyId, JourneyRequest request) {
        return service.updateJourney(journeyId, request);
    }

    public Call<Void> deleteJourney(String journeyId) {
        return service.deleteJourney(journeyId);
    }

    public Call<Journey> updateJourneyStatus(String journeyId) {
        return service.updateJourneyStatus(journeyId);
    }

    public Call<Journey> markPlaceVisited(String journeyId, String placeId) {
        return service.markPlaceVisited(journeyId, placeId);
    }
}
