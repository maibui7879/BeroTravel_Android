package com.example.berotravel20.data.repository;

import android.content.Context;

import com.example.berotravel20.data.api.PlaceStatusService;
import com.example.berotravel20.data.api.RetrofitClient;
import com.example.berotravel20.data.local.SessionManager;
import com.example.berotravel20.data.models.PlaceStatus.PlaceStatus;
import com.example.berotravel20.data.models.PlaceStatus.PlaceStatusRequest;
import com.example.berotravel20.data.models.PlaceStatus.PlaceStatusResponse;

import retrofit2.Call;

public class PlaceStatusRepository {

    private final PlaceStatusService service;

    public PlaceStatusRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        service = RetrofitClient.getClient(sessionManager).create(PlaceStatusService.class);
    }

    public Call<PlaceStatus> getStatusByPlaceId(String placeId) {
        return service.getStatusByPlaceId(placeId);
    }

    public Call<PlaceStatusResponse> createPlaceStatus(PlaceStatusRequest request) {
        return service.createPlaceStatus(request);
    }

    public Call<PlaceStatusResponse> updatePlaceStatus(String id, PlaceStatusRequest request) {
        return service.updatePlaceStatus(id, request);
    }

    public Call<Void> deletePlaceStatus(String id) {
        return service.deletePlaceStatus(id);
    }
}
