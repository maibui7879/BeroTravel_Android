package com.example.berotravel20.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // Singleton Pattern để dùng chung toàn app
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

    // 1. Lưu Token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply(); // Lưu bất đồng bộ
    }

    // 2. Lấy Token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null); // Mặc định trả về null nếu chưa có
    }

    // 3. Xóa Token (Đăng xuất)
    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}