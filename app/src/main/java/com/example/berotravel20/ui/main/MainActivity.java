package com.example.berotravel20.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.worker.NotificationWorker;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Xin quyền thông báo (Bắt buộc cho Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 2. Ép chạy Worker ngay lập tức để check thông báo
        OneTimeWorkRequest immediateRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                "BeroRecursiveCheck",
                ExistingWorkPolicy.REPLACE, // Xóa cái cũ, chạy cái mới này ngay
                immediateRequest
        );
    }
}