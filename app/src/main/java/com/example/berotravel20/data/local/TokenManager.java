package com.example.berotravel20.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";

    private SharedPreferences prefs;
    private static TokenManager instance;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }
    
    public void saveUserSession(String token, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }
    
    public String getUsername() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void clearToken() {
        clearSession();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
