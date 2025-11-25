package com.example.berotravel20.data.api;

import com.example.berotravel20.data.local.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = sessionManager.getToken();

        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}

