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
import java.util.List;

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
        // Bạn có thể format lại ngày tháng ở đây cho đẹp
        holder.tvTime.setText(notification.getCreatedAt());

        // Nếu đã đọc thì ẩn chấm xanh và làm mờ text một chút
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