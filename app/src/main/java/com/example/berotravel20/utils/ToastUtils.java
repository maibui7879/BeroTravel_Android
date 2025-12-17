package com.example.berotravel20.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.berotravel20.R;

public class ToastUtils {

    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    public static final int WARNING = 3;
    public static final int INFO = 4;

    public static void show(Context context, String message, int type) {
        if (context == null) return;

        // Inflate layout
        View layout = LayoutInflater.from(context).inflate(R.layout.layout_custom_toast, null);

        // Ánh xạ View
        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);
        View root = layout.findViewById(R.id.toast_root);

        // Set Text
        text.setText(message);

        // Set Style dựa trên Type
        switch (type) {
            case SUCCESS:
                icon.setImageResource(R.drawable.ic_check_circle); // Icon tích xanh
                icon.setColorFilter(Color.parseColor("#4CAF50")); // Màu xanh lá
                // root.getBackground().setColorFilter(Color.parseColor("#E8F5E9"), PorterDuff.Mode.SRC_IN); // (Tuỳ chọn) Đổi màu nền nhạt
                break;

            case ERROR:
                icon.setImageResource(R.drawable.ic_error); // Icon lỗi (dấu X hoặc chấm than tròn)
                icon.setColorFilter(Color.parseColor("#F44336")); // Màu đỏ
                break;

            case WARNING:
                icon.setImageResource(R.drawable.ic_warning); // Icon tam giác cảnh báo
                icon.setColorFilter(Color.parseColor("#FFC107")); // Màu vàng cam
                break;

            case INFO:
            default:
                icon.setImageResource(R.drawable.ic_info); // Icon chữ i
                icon.setColorFilter(Color.parseColor("#2196F3")); // Màu xanh dương
                break;
        }

        // Tạo Toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    // Các hàm gọi nhanh
    public static void showSuccess(Context context, String message) {
        show(context, message, SUCCESS);
    }

    public static void showError(Context context, String message) {
        show(context, message, ERROR);
    }

    public static void showWarning(Context context, String message) {
        show(context, message, WARNING);
    }
}