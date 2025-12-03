package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.User.AuthPayload;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("/api/users/register")
    Call<AuthPayload.AuthResponse> register(@Body AuthPayload.RegisterRequest request);

    @POST("/api/users/login")
    Call<AuthPayload.AuthResponse> login(@Body AuthPayload.LoginRequest request);
}
