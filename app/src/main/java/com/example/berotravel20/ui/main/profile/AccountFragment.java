package com.example.berotravel20.ui.main.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide; // Nhớ thêm thư viện này
import com.example.berotravel20.R;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.viewmodel.ProfileViewModel;

public class AccountFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvUserName, tvUserEmail, tvFavoriteCount, btnEditProfile;
    private ImageView ivAvatar;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvFavoriteCount = view.findViewById(R.id.tvFavoriteCount);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // 2. Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 3. Lắng nghe dữ liệu
        observeData();

        // 4. Gọi API lấy dữ liệu ngay khi mở màn hình
        viewModel.loadUserProfile(); // Lấy tên, email, ảnh
        viewModel.loadUserStats();   // Lấy danh sách yêu thích để đếm số lượng

        // 5. Xử lý nút Đăng xuất
        btnLogout.setOnClickListener(v -> handleLogout());

        // 6. Xử lý nút Sửa Profile
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void observeData() {
        // Khi có thông tin User về -> Cập nhật UI
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText(user.name);
                tvUserEmail.setText(user.email);

                // Load ảnh Avatar (Dùng Glide)
                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    Glide.with(this)
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.account_icon) // Ảnh mặc định
                            .circleCrop()
                            .into(ivAvatar);
                }
            }
        });

        // Khi có số lượng yêu thích -> Cập nhật số
        viewModel.getFavoriteCount().observe(getViewLifecycleOwner(), count -> {
            tvFavoriteCount.setText(String.valueOf(count));
        });

        // Khi có lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void handleLogout() {
        // Xóa Token
        TokenManager.getInstance(requireContext()).clearToken();

        // Quay về màn đăng nhập
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // Hiển thị hộp thoại sửa tên đơn giản
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật thông tin");

        // Tạo layout cho dialog (gồm 1 ô nhập tên)
        final EditText input = new EditText(requireContext());
        // Lấy tên hiện tại điền vào
        if (tvUserName.getText() != null) {
            input.setText(tvUserName.getText().toString());
        }
        builder.setView(input);

        // Nút Đồng ý
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString();
            // Gọi ViewModel cập nhật (các trường khác để rỗng tạm thời)
            viewModel.updateProfile(newName, "", "");
        });

        // Nút Hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}