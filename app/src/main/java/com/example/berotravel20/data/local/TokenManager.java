package com.example.berotravel20.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {
    private static final String TAG = "TOKEN_MANAGER";

    // Nguồn ưu tiên (Mới)
    private static final String PREF_MY = "MyPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    // Nguồn cũ (Fallback)
    private static final String PREF_BERO = "BeroPrefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";

    private static final String KEY_USERNAME = "user_name";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences myPrefs;
    private final SharedPreferences beroPrefs;
    private static TokenManager instance;

    private TokenManager(Context context) {
        Context appContext = context.getApplicationContext();
        myPrefs = appContext.getSharedPreferences(PREF_MY, Context.MODE_PRIVATE);
        beroPrefs = appContext.getSharedPreferences(PREF_BERO, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    /**
     * Logic ưu tiên: auth_token (MyPrefs) > jwt_token (BeroPrefs)
     */
    public String getToken() {
        // 1. Tìm trong MyPrefs (Key: auth_token)
        String token = myPrefs.getString("auth_token", null);

        // 2. Nếu không thấy, tìm trong BeroPrefs (Key: jwt_token)
        if (token == null || token.isEmpty()) {
            token = beroPrefs.getString("jwt_token", null);
            if (token != null) {
                Log.d("TOKEN_DEBUG", "Found token in BeroPrefs (Legacy)");
            }
        } else {
            Log.d("TOKEN_DEBUG", "Found token in MyPrefs (Current)");
        }

        return token;
    }

    public void saveUserSession(String token, String name, String userId) {
        myPrefs.edit()
                .putString(KEY_AUTH_TOKEN, token)
                .putString(KEY_USERNAME, name)
                .putString(KEY_USER_ID, userId)
                .apply();
        Log.d(TAG, "Đã lưu Session cho: " + name);
    }

    public String getUsername() {
        return myPrefs.getString(KEY_USERNAME, "Người dùng");
    }

    public String getUserId() {
        String id = myPrefs.getString(KEY_USER_ID, null);
        return (id != null) ? id : beroPrefs.getString("user_id", null);
    }

    public void clearSession() {
        myPrefs.edit().clear().apply();
        beroPrefs.edit().clear().apply(); // Xóa sạch cả 2 nguồn khi Logout
        Log.d(TAG, "Logout: Đã xóa toàn bộ Token");
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }
}