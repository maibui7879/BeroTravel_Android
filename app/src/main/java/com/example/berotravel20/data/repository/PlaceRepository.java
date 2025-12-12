package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.PlaceApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceCountResponse;
import com.example.berotravel20.data.model.Place.PlaceImagesRequest;
import com.example.berotravel20.data.model.Place.PlaceResponse; // Import Model hứng response
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

    // --- CÁC HÀM TRẢ VỀ LIST (CẦN BÓC TÁCH DỮ LIỆU) ---

    // 2. Lấy tất cả địa điểm
    public void getAllPlaces(DataCallback<List<Place>> callback) {
        // Không dùng makeCall, tự xử lý để lấy .data
        api.getAllPlaces().enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Bóc tách: Lấy list từ biến "data"
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PlaceResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // 8. Tìm kiếm nearby
    public void searchNearby(double lat, double lng, Integer radius, String name, String category, DataCallback<List<Place>> callback) {
        // Tương tự, gọi API và bóc tách .data
        api.searchNearby(lat, lng, radius, name, category).enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PlaceResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- CÁC HÀM CÒN LẠI (GIỮ NGUYÊN MAKE CALL NẾU API TRẢ VỀ TRỰC TIẾP) ---


    // 3. Tạo mới (Thường backend trả về object vừa tạo, không bọc data)
    public void createPlace(Place.Request request, DataCallback<Place> callback) {
        makeCall(api.createPlace(request), callback);
    }

    // 4. Lấy chi tiết (Thường trả về trực tiếp object Place)
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

    // 7. Cập nhật ảnh
    public void updatePlaceImages(String id, String mainImageUrl, List<String> imgSet, DataCallback<Void> callback) {
        PlaceImagesRequest request = new PlaceImagesRequest(mainImageUrl, imgSet);
        makeCall(api.updatePlaceImages(id, request), callback);
    }
}