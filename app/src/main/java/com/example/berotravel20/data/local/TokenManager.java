package com.example.berotravel20.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "user_name";
    private static final String KEY_USER_ID = "user_id"; // Đã có sẵn

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

    // --- SỬA TẠI ĐÂY: Thêm String userId vào tham số ---
    public void saveUserSession(String token, String name, String userId) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, name);
        editor.putString(KEY_USER_ID, userId); // Bây giờ biến userId đã hợp lệ
        editor.apply();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Bạn");
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    // Cập nhật hàm xóa session để xóa luôn cả ID
    public void clearSession() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_USER_ID); // Xóa nốt ID khi logout
        editor.apply();
    }
}