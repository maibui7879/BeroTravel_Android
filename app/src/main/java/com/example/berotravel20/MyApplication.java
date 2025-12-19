package com.example.berotravel20;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.berotravel20.data.remote.RetrofitClient;
import com.example.berotravel20.worker.NotificationWorker;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        RetrofitClient.getInstance(getApplicationContext());
        setupNotificationWorker();

        boolean isDarkMode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getBoolean("isDarkMode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupNotificationWorker() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                NotificationWorker.class,
                15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "BERO_NOTI",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }
}