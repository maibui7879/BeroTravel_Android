package com.example.berotravel20.data.repository;

import android.util.Log;
import com.example.berotravel20.data.api.FavoriteApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Favorite.FavoriteResponse;
import com.example.berotravel20.data.model.Place.Place; // Import Place
import com.example.berotravel20.data.remote.RetrofitClient;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRepository {

    private final FavoriteApiService apiService;

    public FavoriteRepository() {
        this.apiService = RetrofitClient.getInstance().getFavoriteApi();
    }

    // [SỬA ĐỔI] Đổi tên hàm và kiểu dữ liệu trả về
    public void getMyFavorites(DataCallback<List<Place>> callback) {
        apiService.getMyFavorites().enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trả về danh sách Place đầy đủ
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Lỗi tải danh sách yêu thích: " + response.code();
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void toggleFavorite(String placeId, DataCallback<FavoriteResponse> callback) {
        // ... (Giữ nguyên code cũ của toggle)
        apiService.toggleFavorite(placeId).enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}