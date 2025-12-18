package com.example.berotravel20.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.berotravel20.R;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.ui.main.MainActivity;
import com.example.berotravel20.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etName, etEmail, etPassword;
    private View btnSignUp;
    private TextView tvGoToLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp); // Nút đăng ký
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin);

        // 2. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Lắng nghe kết quả (Tái sử dụng logic của Login vì Register xong cũng trả về Token)
        observeViewModel();

        // 4. Xử lý sự kiện bấm nút Đăng Ký
        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi hàm Register trong ViewModel
            authViewModel.register(name, email, password);
        });

        // 5. Chuyển về màn hình Đăng nhập
        tvGoToLogin.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                // Gọi hàm loadFragment của AuthActivity để chuyển trang
                ((AuthActivity) getActivity()).loadFragment(new LoginFragment());
            }
        });
    }

    private void observeViewModel() {
        // A. Khi Đăng ký thành công (Server trả về Token luôn)
        authViewModel.getLoginResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.token != null) {
                // Lưu token
                TokenManager.getInstance(requireContext()).saveUserSession(response.token, response.name, response.id);

                Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                // Vào thẳng màn hình chính
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                if (getActivity() != null) getActivity().finish();
            }
        });

        // B. Xử lý lỗi
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // C. Xử lý Loading
                authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                    if (isLoading) {
                        btnSignUp.setEnabled(false); // Khóa nút khi đang chạy
                        btnSignUp.setAlpha(0.5f);    // Làm mờ nút
                    } else {
                        btnSignUp.setEnabled(true);  // Mở nút khi chạy xong
                        btnSignUp.setAlpha(1.0f);    // Sáng lại
                    }
                });
    }
}