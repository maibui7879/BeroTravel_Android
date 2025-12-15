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
import com.example.berotravel20.ui.main.MainActivity;
import com.example.berotravel20.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupListeners();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe login response (using same LiveData as login)
        authViewModel.getLoginResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                // Save token
                com.example.berotravel20.utils.SharedPreferencesUtils.getInstance(getContext())
                        .saveToken(response.token);
                if (response.user != null) {
                    com.example.berotravel20.utils.SharedPreferencesUtils.getInstance(getContext())
                            .saveUser(response.user);
                }

                // Navigate to Main
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                if (getActivity() != null)
                    getActivity().finish();
            }
        });

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnRegister.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
            }
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.register(name, email, password);
        });

        tvBackToLogin.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).loadFragment(new LoginFragment());
            }
        });
    }
}