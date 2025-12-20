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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.remote.RetrofitClient;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.main.notification.NotificationActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ các nút chức năng trong fragment_setting
        View btnNotification = view.findViewById(R.id.btn_notification);
        View btnChangePassword = view.findViewById(R.id.btn_change_password);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        // 2. Click mở Activity Thông báo
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), NotificationActivity.class));
            });
        }

        // 3. Click mở Dialog Đổi mật khẩu
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        // 4. Setup Theme (Sáng/Tối)
        setupThemeSwitch(view);

        // 5. Click Đăng xuất
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
                    Toast.makeText(getContext(), "Đã bật chế độ Tối", Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(getContext(), "Đã bật chế độ Sáng", Toast.LENGTH_SHORT).show();
                }
                prefs.edit().putBoolean("isDarkMode", isChecked).apply();
            });
        }
    }

    // --- LOGIC ĐỔI MẬT KHẨU ---
    private void showChangePasswordDialog() {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Ánh xạ lại đúng ID từ file XML dialog_change_password bạn vừa gửi
        EditText etOldPassword = dialog.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialog.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialog.findViewById(R.id.etConfirmPassword);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            // Validate logic
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu mới phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API lưu mật khẩu mới
            changePasswordApi(newPass, dialog);
        });

        dialog.show();
    }

    private void changePasswordApi(String newPass, Dialog dialog) {
        User userUpdate = new User();
        userUpdate.setPassword(newPass); // Nhớ thêm hàm setPassword vào file User.java nhé

        // Sử dụng getRetrofit().create(...) giống như các repo khác của bạn
        RetrofitClient.getInstance(getContext()).getRetrofit().create(com.example.berotravel20.data.api.UserApiService.class)
                .updateProfile(userUpdate)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        if (getContext() == null) return;
        com.example.berotravel20.data.local.TokenManager.getInstance(getContext()).clearSession();
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}