package com.example.berotravel20.ui.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.berotravel20.R;

//Đây là test api. khi làm nhớ sửa lại

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Mặc định load LoginFragment khi mở màn hình này
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_container, new com.example.berotravel20.ui.auth.LoginFragment())
                    .commit();
        }
    }

    // Hàm public để Fragment con có thể gọi khi muốn chuyển trang
    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, fragment)
                .addToBackStack(null) // Cho phép bấm Back để quay lại
                .commit();
    }
}