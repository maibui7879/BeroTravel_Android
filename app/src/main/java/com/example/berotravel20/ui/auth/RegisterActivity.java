package com.example.berotravel20.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.User.AuthPayload;
import com.example.berotravel20.network.ApiClient;
import com.example.berotravel20.network.ApiService;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.ui.main.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Services
        apiService = ApiClient.getClient(this).create(ApiService.class);
        tokenManager = TokenManager.getInstance(this);

        // Bind Views
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);

        // Set Listeners
        btnRegister.setOnClickListener(v -> register());
        tvLoginLink.setOnClickListener(v -> {
            finish(); // Go back to Login Activity
        });
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthPayload.RegisterRequest request = new AuthPayload.RegisterRequest(name, email, password);

        apiService.register(request).enqueue(new Callback<AuthPayload.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthPayload.AuthResponse> call, Response<AuthPayload.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthPayload.AuthResponse body = response.body();

                    // Save Token & User Info
                    tokenManager.saveUserSession(body.token, body.name);
                    tokenManager.saveUserId(body.id);

                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                    // Navigate to Main
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration Failed: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthPayload.AuthResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
