package com.example.berotravel20.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.berotravel20.R;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.main.MainActivity;
import com.example.berotravel20.viewmodel.AuthViewModel;

//Đây là test api. khi làm nhớ sửa lại
public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvGoToRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo View
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister);

        // 2. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Lắng nghe (Observe) sự kiện từ ViewModel
        observeViewModel();

        // 4. Xử lý sự kiện click nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi ViewModel để login
            authViewModel.login(email, password);
        });

        // 5. Chuyển sang màn hình Đăng ký
        tvGoToRegister.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).loadFragment(new RegisterFragment());
            }
        });
    }

    private void observeViewModel() {
        // A. Lắng nghe kết quả login thành công
        authViewModel.getLoginResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                // Login thành công!
                Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // Lưu token vào SharedPreferences
                String token = response.token;
                // Nếu AuthResponse trả về User object
                com.example.berotravel20.data.model.User.User user = response.user;

                com.example.berotravel20.utils.SharedPreferencesUtils.getInstance(getContext()).saveToken(token);
                if (user != null) {
                    com.example.berotravel20.utils.SharedPreferencesUtils.getInstance(getContext()).saveUser(user);
                }

                // Chuyển sang MainActivity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);

                // Đóng AuthActivity để user không back lại được
                if (getActivity() != null)
                    getActivity().finish();
            }
        });

        // B. Lắng nghe trạng thái loading
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
            }
        });

        // C. Lắng nghe lỗi
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}