package com.example.berotravel20.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.berotravel20.data.model.User.User;
import com.google.gson.Gson;

public class SharedPreferencesUtils {
    private static final String PREF_NAME = "BeroTravelPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "user_info";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static SharedPreferencesUtils instance;

    private SharedPreferencesUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPreferencesUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesUtils(context.getApplicationContext());
        }
        return instance;
    }

    // Token Management
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // User Management
    public void saveUser(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    public User getUser() {
        String json = sharedPreferences.getString(KEY_USER, null);
        if (json != null) {
            return new Gson().fromJson(json, User.class);
        }
        return null;
    }

    public void clearUser() {
        editor.remove(KEY_USER);
        editor.apply();
    }

    // Clear All (Logout)
    public void logout() {
        clearToken();
        clearUser();
    }
}
