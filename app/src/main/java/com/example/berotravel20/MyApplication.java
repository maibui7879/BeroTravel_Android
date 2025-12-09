package com.example.berotravel20;

import android.app.Application;
import com.example.berotravel20.data.remote.RetrofitClient;
import androidx.appcompat.app.AppCompatDelegate;
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Truyền Context vào đây để RetrofitClient có thể đọc SharedPreferences
        RetrofitClient.getInstance(getApplicationContext());
    }
}