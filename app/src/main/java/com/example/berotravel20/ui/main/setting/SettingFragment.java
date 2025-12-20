package com.example.berotravel20.ui.main.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.remote.RetrofitClient;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.main.notification.NotificationActivity;
import com.example.berotravel20.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    public SettingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnNotification = view.findViewById(R.id.btn_notification);
        View btnChangePassword = view.findViewById(R.id.btn_change_password);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), NotificationActivity.class));
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        setupThemeSwitch(view);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
    }

    private void setupThemeSwitch(View view) {
        SwitchCompat switchTheme = view.findViewById(R.id.switchTheme);

        if (switchTheme != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
            boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
            switchTheme.setChecked(isDarkMode);

            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                prefs.edit().putBoolean("isDarkMode", isChecked).apply();
            });
        }
    }

    private void showChangePasswordDialog() {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etOldPassword = dialog.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialog.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialog.findViewById(R.id.etConfirmPassword);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                showCustomNotifyDialog("Thiếu thông tin", "Vui lòng nhập đầy đủ các trường mật khẩu.", false);
                return;
            }

            if (newPass.length() < 6) {
                showCustomNotifyDialog("Mật khẩu yếu", "Mật khẩu mới phải từ 6 ký tự trở lên.", false);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                showCustomNotifyDialog("Lỗi xác nhận", "Mật khẩu mới và xác nhận không khớp nhau.", false);
                return;
            }

            changePasswordApi(newPass, dialog);
        });

        dialog.show();
    }

    private void changePasswordApi(String newPass, Dialog inputDialog) {
        User userUpdate = new User();
        userUpdate.setPassword(newPass);

        RetrofitClient.getInstance(getContext()).getRetrofit().create(com.example.berotravel20.data.api.UserApiService.class)
                .updateProfile(userUpdate)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            inputDialog.dismiss();
                            showCustomNotifyDialog("Thành công", "Mật khẩu của bạn đã được thay đổi!", true);
                        } else {
                            showCustomNotifyDialog("Thất bại", "Không thể cập nhật mật khẩu. Mã lỗi: " + response.code(), false);
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        showCustomNotifyDialog("Lỗi kết nối", "Vui lòng kiểm tra internet và thử lại.", false);
                    }
                });
    }

    /**
     * Hàm hiển thị Custom Dialog thông báo hợp nhất với phong cách của app
     */
    private void showCustomNotifyDialog(String title, String message, boolean isSuccess) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_success, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvMsg = dialogView.findViewById(R.id.tv_dialog_message);
        ImageView ivIcon = dialogView.findViewById(R.id.iv_dialog_icon);
        Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);

        tvTitle.setText(title);
        tvMsg.setText(message);

        if (!isSuccess) {
            // Đổi icon và màu nút sang màu Đỏ nếu là thất bại/cảnh báo
            ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
            btnConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
        }

        btnConfirm.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }

        dialog.show();
    }

    private void logout() {
        if (getContext() == null) return;
        com.example.berotravel20.data.local.TokenManager.getInstance(getContext()).clearSession();
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}