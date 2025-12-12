package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.ORS.ORSResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ORSService {
    // Endpoint chỉ đường cho xe hơi
    @GET("v2/directions/driving-car")
    Call<ORSResponse> getDirections(
            @Query("api_key") String apiKey,
            @Query("start") String start, // format: "lng,lat"
            @Query("end") String end      // format: "lng,lat"
    );
}