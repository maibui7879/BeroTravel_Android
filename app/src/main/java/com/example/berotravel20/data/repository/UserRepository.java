package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.RetrofitClient;
import com.example.berotravel20.data.api.UserService;
import com.example.berotravel20.data.local.SessionManager;
import com.example.berotravel20.data.models.User.LoginRequest;
import com.example.berotravel20.data.models.User.LoginResponse;
import com.example.berotravel20.data.models.User.RegisterRequest;
import com.example.berotravel20.data.models.User.RegisterResponse;

import retrofit2.Call;

public class UserRepository {

    private final UserService service;

    public UserRepository(SessionManager sessionManager) {
        service = RetrofitClient.getClient(sessionManager).create(UserService.class);
    }

    public Call<LoginResponse> login(LoginRequest request) {
        return service.login(request);
    }

    public Call<RegisterResponse> register(RegisterRequest request) {
        return service.register(request);
    }

    public Call<LoginResponse> getProfile() {
        return service.getProfile();
    }
}
