package com.example.berotravel20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Notification.Notification;

// Nhớ thêm mấy cái import này để không bị báo đỏ ở hàm format
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvMessage.setText(notification.getMessage());

        // --- SỬ DỤNG HÀM FORMAT TẠI ĐÂY ---
        String rawTime = notification.getCreatedAt();
        holder.tvTime.setText(formatTimeLegacy(rawTime));

        if (notification.isRead()) {
            holder.ivUnreadDot.setVisibility(View.GONE);
            holder.tvMessage.setAlpha(0.6f);
        } else {
            holder.ivUnreadDot.setVisibility(View.VISIBLE);
            holder.tvMessage.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notification, position));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public String formatTimeLegacy(String rawDate) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = parser.parse(rawDate);

            // Định dạng hiển thị dễ nhìn cho người dùng
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            return "Vừa xong";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView ivUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            ivUnreadDot = itemView.findViewById(R.id.iv_unread_dot);
        }
    }
}