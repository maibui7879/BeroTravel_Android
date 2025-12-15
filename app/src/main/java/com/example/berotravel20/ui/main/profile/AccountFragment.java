package com.example.berotravel20.ui.main.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.utils.SharedPreferencesUtils;

public class AccountFragment extends Fragment {

    private LinearLayout layoutNotLoggedIn;
    private LinearLayout layoutLoggedIn;
    private Button btnLogin;
    private Button btnLogout;
    private TextView tvUserInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    private void initViews(View view) {
        layoutNotLoggedIn = view.findViewById(R.id.layoutNotLoggedIn);
        layoutLoggedIn = view.findViewById(R.id.layoutLoggedIn);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvUserInfo = view.findViewById(R.id.tvUserInfo);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferencesUtils.getInstance(getContext()).logout();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            checkLoginStatus();
        });
    }

    private void checkLoginStatus() {
        boolean isLoggedIn = SharedPreferencesUtils.getInstance(getContext()).isLoggedIn();
        if (isLoggedIn) {
            layoutNotLoggedIn.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.VISIBLE);

            com.example.berotravel20.data.model.User.User user = SharedPreferencesUtils.getInstance(getContext())
                    .getUser();
            if (user != null && user.name != null) {
                tvUserInfo.setText("Xin chào, " + user.name);
            } else {
                tvUserInfo.setText("Xin chào, User");
            }
        } else {
            layoutNotLoggedIn.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
        }
    }
}
