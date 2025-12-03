package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.AuthApiService;
import com.example.berotravel20.data.api.UserApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Notification.Notification;
import com.example.berotravel20.data.model.User.AuthPayload;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.remote.RetrofitClient;

import java.util.List;

public class UserRepository extends BaseRepository {
    // Lấy instance của các API Service
    private AuthApiService authApi = RetrofitClient.getInstance().getAuthApi();
    private UserApiService userApi = RetrofitClient.getInstance().getUserApi();

    // --- KHU VỰC AUTHENTICATION (Đăng nhập/Đăng ký) ---

    public void login(String email, String pass, DataCallback<AuthPayload.AuthResponse> callback) {
        makeCall(authApi.login(new AuthPayload.LoginRequest(email, pass)), callback);
    }

    public void register(String name, String email, String pass, DataCallback<AuthPayload.AuthResponse> callback) {
        makeCall(authApi.register(new AuthPayload.RegisterRequest(name, email, pass)), callback);
    }

    // --- KHU VỰC USER PROFILE ---

    public void getProfile(DataCallback<User> callback) {
        makeCall(userApi.getProfile(), callback);
    }

    // Bổ sung hàm cập nhật thông tin User
    public void updateProfile(User user, DataCallback<User> callback) {
        makeCall(userApi.updateProfile(user), callback);
    }

    // --- KHU VỰC NOTIFICATION ---
    // (Vì API Service bạn để getNotifications ở đây nên Repo cũng triển khai ở đây)
    public void getNotifications(DataCallback<List<Notification>> callback) {
        makeCall(userApi.getNotifications(), callback);
    }

    // --- KHU VỰC FAVORITES (Yêu thích) ---

    // Thêm/Xóa yêu thích
    public void toggleFavorite(String placeId, DataCallback<Void> callback) {
        makeCall(userApi.toggleFavorite(placeId), callback);
    }

    // Lấy danh sách các Place ID đã yêu thích
    public void getFavorites(DataCallback<List<String>> callback) {
        makeCall(userApi.getFavorites(), callback);
    }
}