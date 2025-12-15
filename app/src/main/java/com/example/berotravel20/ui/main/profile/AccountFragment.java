package com.example.berotravel20.ui.main.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Đảm bảo khai báo đúng Button nếu bạn dùng Button trong XML
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.ui.auth.AuthActivity;

public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dùng layout mới đã sửa (fragment_profile)
        return inflater.inflate(R.layout.fragment_profile, container, false); //
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ nút Logout
        // Dùng Button nếu XML dùng <Button>, dùng View nếu dùng <LinearLayout>
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Gán sự kiện click
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                handleLogout();
            });
        }

        // 3. Tải thông tin Profile (TODO: Bạn sẽ làm sau khi có API User Profile)
        // TextView tvUserName = view.findViewById(R.id.tvUserName);
        // tvUserName.setText("Tên người dùng đã đăng nhập");
    }

    // Hàm xử lý Đăng xuất
    private void handleLogout() {
        // 1. Xóa Token (Sử dụng Singleton getInstance)
        TokenManager.getInstance(requireContext()).clearToken(); //

        // 2. Thông báo
        Toast.makeText(getContext(), "Đăng xuất thành công! Hẹn gặp lại.", Toast.LENGTH_SHORT).show();

        // 3. Chuyển về màn hình AuthActivity (Login/Register)
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        // Thêm flags để xóa sạch Back Stack (không cho người dùng quay lại màn hình chính)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Đóng MainActivity
        if (getActivity() != null) getActivity().finish();
    }
}