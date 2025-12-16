package com.example.berotravel20.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.berotravel20.R;
import com.example.berotravel20.ui.auth.AuthActivity;

public class RequestLoginDialog extends DialogFragment {

    private RequestLoginListener listener;

    public interface RequestLoginListener {
        void onLoginClick();
        void onCancelClick();
    }

    public static RequestLoginDialog newInstance() {
        return new RequestLoginDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof RequestLoginListener) {
            listener = (RequestLoginListener) getParentFragment();
        }
    }

    public void setListener(RequestLoginListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_request_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ nút
        View btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvCancel = view.findViewById(R.id.tvCancel);

        // Xử lý Đăng nhập
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            startActivity(intent);

            if (listener != null) listener.onLoginClick();
            dismiss();
        });

        // Xử lý Hủy
        tvCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancelClick();
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Cấu hình Dialog để hiển thị đẹp (Bo tròn, Full width nhưng có margin)
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // 1. Làm trong suốt nền mặc định của Android để thấy được bo tròn của XML
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // 2. Chỉnh kích thước (chiếm 85% màn hình)
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
                window.setAttributes(params);
            }
        }
    }
}