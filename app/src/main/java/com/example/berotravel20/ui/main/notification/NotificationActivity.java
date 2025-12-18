package com.example.berotravel20.ui.main.notification;

import static com.example.berotravel20.utils.DateUtils.formatTimeLegacy;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.NotificationAdapter;
import com.example.berotravel20.data.api.NotificationApiService;
import com.example.berotravel20.data.model.Notification.Notification;
import com.example.berotravel20.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> mList;
    private NotificationApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Init View
        rvNotifications = findViewById(R.id.rv_notifications);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Init Data & Adapter
        mList = new ArrayList<>();
        adapter = new NotificationAdapter(this, mList, this::showNotificationDialog);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        // Init API
        apiService = RetrofitClient.getInstance().getNotificationApi();

        loadNotifications();
    }

    private void loadNotifications() {
        // Giả sử RetrofitClient đã config Header Authorization (Token)
        apiService.getNotifications().enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mList.clear();
                    mList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNotificationDialog(Notification notification, int position) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notification_detail);

        // 1. Lấy dữ liệu và Format
        // Format cái giờ "lỏ" sang giờ đẹp
        String beautifulTime = formatTimeLegacy(notification.getCreatedAt());

        // Logic "gánh team": Thay cái ID lỏ bằng tên thật cho các địa điểm hay dùng để demo
        String message = notification.getMessage();
        if (message != null) {
            if (message.contains("68c90b83cd2412021daaab61")) {
                message = message.replace("68c90b83cd2412021daaab61", "Hoàng Thành Thăng Long");
            } else if (message.contains("68c90c09cd2412021daab293")) {
                message = message.replace("68c90c09cd2412021daab293", "Văn Miếu Quốc Tử Giám");
            }
        }

        // 2. Map view trong dialog và set data
        TextView tvMessage = dialog.findViewById(R.id.tv_dialog_message);
        TextView tvTime = dialog.findViewById(R.id.tv_dialog_time);
        Button btnClose = dialog.findViewById(R.id.btn_close_dialog);

        tvMessage.setText(message); // Đã thay ID bằng tên (nếu khớp)
        tvTime.setText(beautifulTime); // FIX: Dùng cái beautifulTime đã format nhé!

        // 3. Cấu hình Window (Nên làm trước khi show() cho mượt)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        // 4. Gọi API đánh dấu đã đọc
        if (!notification.isRead()) {
            markAsRead(notification, position);
        }
    }

    private void markAsRead(Notification notification, int position) {
        apiService.markAsRead(notification.getId()).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                if (response.isSuccessful()) {
                    // Cập nhật UI local
                    notification.setRead(true);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<Notification> call, Throwable t) {
                android.util.Log.e("NotificationCheck", "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}