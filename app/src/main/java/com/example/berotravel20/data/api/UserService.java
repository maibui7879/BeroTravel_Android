package com.example.berotravel20.data.api;

import com.example.berotravel20.data.models.User.LoginRequest;
import com.example.berotravel20.data.models.User.LoginResponse;
import com.example.berotravel20.data.models.User.RegisterRequest;
import com.example.berotravel20.data.models.User.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserService {

    @POST("/users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/users/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("/users/profile")
    Call<LoginResponse> getProfile();
}
