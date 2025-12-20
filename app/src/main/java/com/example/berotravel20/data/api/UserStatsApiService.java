package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Stats.UserStatsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserStatsApiService {
    // Lấy thống kê bản thân
    @GET("api/stats")
    Call<UserStatsResponse> getMyStats();

    // Lấy thống kê user khác
    @GET("api/stats/{userId}")
    Call<UserStatsResponse> getUserStats(@Path("userId") String userId);
}