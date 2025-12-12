package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.ORS.ORSRequest; // Import model vừa tạo
import com.example.berotravel20.data.model.ORS.ORSResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ORSService {
    // Chuyển sang POST để hỗ trợ language
    // Endpoint: v2/directions/{profile}/geojson
    @POST("v2/directions/{profile}/geojson")
    Call<ORSResponse> getDirections(
            @Path("profile") String profile,
            @Query("api_key") String apiKey,
            @Body ORSRequest body // Gửi body chứa toạ độ và ngôn ngữ
    );
}