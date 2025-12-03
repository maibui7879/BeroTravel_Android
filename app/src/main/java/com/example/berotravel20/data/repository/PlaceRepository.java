package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.PlaceApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceCountResponse;
import com.example.berotravel20.data.model.Place.PlaceImagesRequest;
import com.example.berotravel20.data.remote.RetrofitClient;

import java.util.List;

public class PlaceRepository extends BaseRepository {
    private PlaceApiService api;

    public PlaceRepository() {
        this.api = RetrofitClient.getInstance().getPlaceApi();
    }

    // 1. Lấy tổng số lượng
    public void getPlacesCount(DataCallback<PlaceCountResponse> callback) {
        makeCall(api.getPlacesCount(), callback);
    }

    // 2. Lấy tất cả địa điểm
    public void getAllPlaces(DataCallback<List<Place>> callback) {
        makeCall(api.getAllPlaces(), callback);
    }

    // 3. Tạo mới
    public void createPlace(Place.Request request, DataCallback<Place> callback) {
        makeCall(api.createPlace(request), callback);
    }

    // 4. Lấy chi tiết
    public void getPlaceById(String id, DataCallback<Place> callback) {
        makeCall(api.getPlace(id), callback);
    }

    // 5. Cập nhật thông tin
    public void updatePlace(String id, Place.Request request, DataCallback<Place> callback) {
        makeCall(api.updatePlace(id, request), callback);
    }

    // 6. Xóa địa điểm
    public void deletePlace(String id, DataCallback<Void> callback) {
        makeCall(api.deletePlace(id), callback);
    }

    // 7. Cập nhật ảnh (Chính & Phụ)
    public void updatePlaceImages(String id, String mainImageUrl, List<String> imgSet, DataCallback<Void> callback) {
        PlaceImagesRequest request = new PlaceImagesRequest(mainImageUrl, imgSet);
        makeCall(api.updatePlaceImages(id, request), callback);
    }

    // 8. Tìm kiếm nearby
    public void searchNearby(double lat, double lng, Integer radius, String keyword, String category, DataCallback<List<Place>> callback) {
        makeCall(api.searchNearby(lat, lng, radius, keyword, category), callback);
    }
}