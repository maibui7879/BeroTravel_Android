package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.PlaceService;
import com.example.berotravel20.data.api.RetrofitClient;
import com.example.berotravel20.data.local.SessionManager;
import com.example.berotravel20.data.models.Place.Place;
import com.example.berotravel20.data.models.Place.PlaceRequest;
import com.example.berotravel20.data.models.Place.PlaceResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class PlaceRepository {

    private final PlaceService service;

    public PlaceRepository(SessionManager sessionManager) {
        service = RetrofitClient.getClient(sessionManager).create(PlaceService.class);
    }

    // Lấy tổng số địa điểm
    public Call<Map<String, Integer>> getPlaceCount() {
        return service.getPlaceCount();
    }

    // Lấy tất cả địa điểm
    public Call<List<Place>> getAllPlaces() {
        return service.getAllPlaces();
    }

    // Lấy địa điểm theo ID
    public Call<Place> getPlaceById(String placeId) {
        return service.getPlaceById(placeId);
    }

    // Tìm địa điểm gần vị trí hiện tại
    public Call<List<Place>> getNearbyPlaces(double latitude, double longitude, double radius) {
        return service.getNearbyPlaces(latitude, longitude, radius);
    }

    // Tạo địa điểm mới
    public Call<PlaceResponse> createPlace(PlaceRequest request) {
        return service.createPlace(request);
    }

    // Cập nhật địa điểm theo ID
    public Call<PlaceResponse> updatePlace(String placeId, PlaceRequest request) {
        return service.updatePlace(placeId, request);
    }

    // Xóa địa điểm theo ID
    public Call<Void> deletePlace(String placeId) {
        return service.deletePlace(placeId);
    }

    // Cập nhật ảnh chính và ảnh phụ cho địa điểm
    public Call<PlaceResponse> updatePlaceImages(String placeId, Map<String, Object> images) {
        return service.updatePlaceImages(placeId, images);
    }
}
