package com.example.berotravel20.data.model.User;

import com.google.gson.annotations.SerializedName;

public class AuthPayload {
    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;

        public RegisterRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }

    public static class AuthResponse {
        public String token;
        public String message;

        // Flattened User fields matching backend response
        @SerializedName("_id")
        public String id;
        public String name;
        public String email;
        public String role;
        @SerializedName("avatar_url")
        public String avatarUrl;
    }
}