package com.example.berotravel20;

import android.app.Application;
import com.example.berotravel20.data.remote.RetrofitClient;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Truyền Context vào đây để RetrofitClient có thể đọc SharedPreferences
        RetrofitClient.getInstance(getApplicationContext());
    }
}