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
        // Liên kết với file xml vừa tạo
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Xử lý nút Thông báo -> Mở Activity Thông báo
        view.findViewById(R.id.btn_notification).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });

        // 2. Xử lý nút Đổi mật khẩu -> Hiện Dialog
        view.findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // 3. Xử lý nút Chế độ tối (Theme)
        setupThemeSwitch(view);

        // 4. Xử lý Đăng xuất
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            logout();
        });
    }

    // --- LOGIC ĐỔI MẬT KHẨU ---
    private void showChangePasswordDialog() {
        if (getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);

        // Làm trong suốt nền dialog để bo góc đẹp
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText edtNewPass = dialog.findViewById(R.id.edt_new_pass);
        Button btnSave = dialog.findViewById(R.id.btn_save_pass);

        btnSave.setOnClickListener(v -> {
            String newPass = edtNewPass.getText().toString().trim();
            if (newPass.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API đổi pass
            changePasswordApi(newPass, dialog);
        });

        dialog.show();
        // Set chiều ngang dialog cho full màn hình chút
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void changePasswordApi(String newPass, Dialog dialog) {
        User userUpdate = new User();
        userUpdate.password = newPass;

        // Gọi API update profile nhưng chỉ gửi password
        RetrofitClient.getInstance(getContext()).getUserApi().updateProfile(userUpdate)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- LOGIC THEME (SÁNG/TỐI) ---
    private void setupThemeSwitch(View view) {
        SwitchCompat switchTheme = view.findViewById(R.id.switch_theme);

        // Kiểm tra chế độ hiện tại để set trạng thái nút switch
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchTheme.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(getContext(), "Đã bật chế độ Tối", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(getContext(), "Đã bật chế độ Sáng", Toast.LENGTH_SHORT).show();
            }
            // Lưu ý: App sẽ tự load lại màn hình để áp dụng màu mới
        });
    }

    // --- LOGIC ĐĂNG XUẤT ---
    private void logout() {
        if (getContext() == null) return;

        // 1. Xóa token đã lưu
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // Xóa sạch dữ liệu đăng nhập
        editor.apply();

        // 2. Chuyển về màn hình Đăng nhập (AuthActivity)
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        // Cờ này để xóa hết các Activity cũ, ngăn người dùng bấm Back quay lại
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}