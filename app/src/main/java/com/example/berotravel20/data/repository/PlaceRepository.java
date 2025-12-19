package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.PlaceApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceImagesRequest;
import com.example.berotravel20.data.model.Place.PlaceResponse;
import com.example.berotravel20.data.remote.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceRepository extends BaseRepository {
    private PlaceApiService api;

    public PlaceRepository() {
        this.api = RetrofitClient.getInstance().getPlaceApi();
    }

    // 1. Get All (Không phân trang hoặc mặc định server)
    public void getAllPlaces(DataCallback<List<Place>> callback) {
        api.getAllPlaces().enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("Lỗi server: " + response.code());
                }
            }
            @Override public void onFailure(Call<PlaceResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // 2. [CẬP NHẬT] Search Nearby (Nhận thêm int limit)
    public void searchNearby(double lat, double lng, Integer radius, String name, String category, Integer page, Integer limit, DataCallback<PlaceResponse> callback) {
        // Retrofit sẽ tự động bỏ qua các param có giá trị null
        api.searchNearby(lat, lng, radius, name, category, page, limit).enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError(response.message());
            }
            @Override public void onFailure(Call<PlaceResponse> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void createPlace(Place.Request request, DataCallback<Place> callback) { makeCall(api.createPlace(request), callback); }
    public void getPlaceById(String id, DataCallback<Place> callback) { makeCall(api.getPlace(id), callback); }
    public void updatePlace(String id, Place.Request request, DataCallback<Place> callback) { makeCall(api.updatePlace(id, request), callback); }
    public void deletePlace(String id, DataCallback<Void> callback) { makeCall(api.deletePlace(id), callback); }
    public void updatePlaceImages(String id, String mainImageUrl, List<String> imgSet, DataCallback<Void> callback) {
        PlaceImagesRequest request = new PlaceImagesRequest(mainImageUrl, imgSet);
        makeCall(api.updatePlaceImages(id, request), callback);
    }
}