package com.example.berotravel20.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import com.example.berotravel20.R;
import com.example.berotravel20.ui.main.MainActivity;   // <-- bắt buộc phải KHỚP với package thật

import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.ui.auth.AuthActivity;   // Import màn hình đăng nhập

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // 1. Lấy Token từ trong máy ra kiểm tra
            TokenManager tokenManager = TokenManager.getInstance(this);
            String token = tokenManager.getToken();

            Intent intent;
            if (token != null && !token.isEmpty()) {
                // 2a. CÓ TOKEN -> Vào màn hình chính (MainActivity)
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // 2b. KHÔNG CÓ TOKEN -> Đá về màn hình đăng nhập (AuthActivity)
                intent = new Intent(SplashActivity.this, AuthActivity.class);
            }

            // 3. Chuyển màn hình và đóng Splash lại
            startActivity(intent);
            finish();
        }, 1500);
    }
}
