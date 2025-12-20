package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.UserStatsApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Stats.UserStatsResponse;
import com.example.berotravel20.data.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserStatsRepository {
    private final UserStatsApiService apiService;

    public UserStatsRepository() {
        // Giả sử bạn có hàm getUserStatsApi() trong RetrofitClient như lưu ý ở trên
        // Hoặc bạn có thể gọi: RetrofitClient.getInstance().getRetrofit().create(UserStatsApiService.class);
        this.apiService = RetrofitClient.getInstance().getUserStatsApi();
    }

    // 1. Lấy thống kê của chính mình
    public void getMyStats(DataCallback<UserStatsResponse> callback) {
        apiService.getMyStats().enqueue(new Callback<UserStatsResponse>() {
            @Override
            public void onResponse(Call<UserStatsResponse> call, Response<UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi lấy thống kê: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserStatsResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // 2. Lấy thống kê của user bất kỳ
    public void getUserStats(String userId, DataCallback<UserStatsResponse> callback) {
        apiService.getUserStats(userId).enqueue(new Callback<UserStatsResponse>() {
            @Override
            public void onResponse(Call<UserStatsResponse> call, Response<UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không tìm thấy user hoặc lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserStatsResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}