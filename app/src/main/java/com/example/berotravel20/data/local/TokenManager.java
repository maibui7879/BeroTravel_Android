package com.example.berotravel20.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "user_name";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private static TokenManager instance;

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    // --- CÁCH 1: DÙNG CHO LOGIN/REGISTER (Lưu cả tên để hiện ngoài Splash) ---
    public void saveUserSession(String token, String name) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, name);
        editor.apply();
    }

    // --- CÁCH 2: HÀM CŨ (Giữ lại để không bị lỗi Code cũ) ---
    // Nếu gọi hàm này, tên sẽ mặc định là "Bạn" hoặc null
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        // Không lưu đè tên nếu chỉ muốn update token
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        // Mặc định trả về "Bạn" nếu không tìm thấy tên
        return prefs.getString(KEY_USERNAME, "Bạn");
    }

    public void clearSession() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USERNAME);
        editor.apply();
    }
}