package com.example.berotravel20.ui.main.notification;

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
        // 1. Hiển thị Dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notification_detail);

        // Map view trong dialog
        TextView tvMessage = dialog.findViewById(R.id.tv_dialog_message);
        TextView tvTime = dialog.findViewById(R.id.tv_dialog_time);
        Button btnClose = dialog.findViewById(R.id.btn_close_dialog);

        tvMessage.setText(notification.getMessage());
        tvTime.setText(notification.getCreatedAt());

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        // 2. Gọi API đánh dấu đã đọc nếu chưa đọc
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