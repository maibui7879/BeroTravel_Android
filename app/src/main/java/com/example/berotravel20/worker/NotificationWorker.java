package com.example.berotravel20.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.berotravel20.R;
import com.example.berotravel20.data.api.NotificationApiService;
import com.example.berotravel20.data.model.Notification.Notification;
import com.example.berotravel20.data.remote.RetrofitClient;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit2.Response;

public class NotificationWorker extends Worker {
    private static final String TAG = "BeroLog";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker đã bắt đầu chạy...");

        // --- THÔNG BÁO TEST (Xóa dòng này sau khi thấy nó hiện) ---
        sendPushNotification("Hệ thống Bero", "Code Android đang chạy rất tốt!");

        try {
            NotificationApiService api = RetrofitClient.getInstance(getApplicationContext())
                    .getRetrofit().create(NotificationApiService.class);

            Response<List<Notification>> response = api.getNotifications().execute();

            if (response.isSuccessful() && response.body() != null) {
                List<Notification> list = response.body();
                Log.d(TAG, "Lấy được " + list.size() + " thông báo từ Server");

                for (Notification n : list) {
                    // Log để bạn check xem Server trả về true hay false
                    Log.d(TAG, "Nội dung: " + n.getMessage() + " | Đã đọc: " + n.isRead());

                    if (!n.isRead()) {
                        sendPushNotification("BeroTravel", n.getMessage());
                        break;
                    }
                }
            } else {
                Log.e(TAG, "Lỗi kết nối API: " + response.code());
            }

            // Tự động lên lịch chạy lại sau 5 phút (Chữa cháy Real-time)
            OneTimeWorkRequest nextRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(5, TimeUnit.MINUTES)
                    .build();

            WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                    "BeroRecursiveCheck",
                    ExistingWorkPolicy.REPLACE,
                    nextRequest
            );

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi hệ thống: " + e.getMessage());
            return Result.retry();
        }
    }

    private void sendPushNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "BERO_FIX_CHANNEL_V1"; // ID mới hoàn toàn

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Bero Updates", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ai_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        }
    }
}