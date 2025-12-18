package com.example.berotravel20.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect; // Thêm cái này
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.berotravel20.R;
import com.example.berotravel20.data.api.NotificationApiService;
import com.example.berotravel20.data.model.Notification.Notification;
import com.example.berotravel20.data.remote.RetrofitClient;
import java.util.List;
import retrofit2.Response;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Lấy API (Đảm bảo RetrofitClient đã có hàm getRetrofit() như mình bàn nãy)
            NotificationApiService api = RetrofitClient.getInstance(getApplicationContext())
                    .getRetrofit().create(NotificationApiService.class);

            Response<List<Notification>> response = api.getNotifications().execute();

            if (response.isSuccessful() && response.body() != null) {
                List<Notification> list = response.body();
                for (Notification n : list) {
                    if (!n.isRead()) {
                        sendPushNotification("Bero Travel", n.getMessage());
                        break;
                    }
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void sendPushNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "BERO_CHANNEL_HIGH"; // Đổi ID channel để Android tạo lại cái mới xịn hơn

        // 1. Cấu hình Channel mức HIGH (Để nó hiện banner và rung)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Bero Updates", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo du lịch quan trọng");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500}); // Rung nhịp điệu
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 2. Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ai_icon) // Dùng icon AI cho đẹp
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao cho máy đời thấp
                .setDefaults(NotificationCompat.DEFAULT_ALL)   // Dùng âm thanh/rung mặc định
                .setAutoCancel(true);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }

        // 3. Rung thủ công
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