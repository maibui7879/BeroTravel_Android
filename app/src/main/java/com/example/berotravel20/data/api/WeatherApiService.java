package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Weather.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units, // Để lấy độ C thì truyền "metric"
            @Query("lang") String lang    // Truyền "vi" để lấy tiếng Việt
    );
}