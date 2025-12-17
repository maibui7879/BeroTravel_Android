package com.example.berotravel20.data.model.User;

import com.google.gson.annotations.SerializedName;

public class AuthPayload {

    // Request Login (Gửi đi)
    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    // Request Register (Gửi đi)
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

    // [QUAN TRỌNG] Response (Nhận về) - SỬA LẠI CHO KHỚP JSON
    public static class AuthResponse {
        public String token;

        @SerializedName("_id")
        public String id;     // Mapping với "_id"

        public String name;   // Mapping với "name" (Ngang hàng token)
        public String role;   // Mapping với "role"

        // public User user;  <-- XÓA DÒNG NÀY (Vì JSON không có object user lồng vào)
    }
}