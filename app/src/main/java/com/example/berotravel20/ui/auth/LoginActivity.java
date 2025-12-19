package com.example.berotravel20.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.berotravel20.R;
import com.example.berotravel20.models.LoginRequest;
import com.example.berotravel20.data.model.User.AuthPayload;
import com.example.berotravel20.network.ApiClient;
import com.example.berotravel20.network.ApiService;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.ui.main.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ApiService apiService;
    private TokenManager tokenManager;
    private TextView tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);
        apiService = ApiClient.getClient(this).create(ApiService.class);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterLink = findViewById(R.id.tv_register_link);

        btnLogin.setOnClickListener(v -> login());
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthPayload.LoginRequest request = new AuthPayload.LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<AuthPayload.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthPayload.AuthResponse> call, Response<AuthPayload.AuthResponse> response) {
                android.util.Log.d("LoginActivity", "Login response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    AuthPayload.AuthResponse body = response.body();

                    if (body.token != null && body.id != null) {
                        android.util.Log.d("LoginActivity", "Login successful. Token: " + body.token);

                        // Unified Token Saving
                        tokenManager.saveUserSession(body.token, body.name);
                        tokenManager.saveUserId(body.id);

                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthPayload.AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
