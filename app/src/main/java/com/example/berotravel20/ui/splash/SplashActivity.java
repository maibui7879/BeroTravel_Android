package com.example.berotravel20.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.berotravel20.R;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.common.BaseActivity;

public class SplashActivity extends AppCompatActivity {

    // Views Splash
    private LinearLayout horizontalGroup;
    private View ivLogoBero;
    private TextView tvTravel;
    private TextView tvSlogan;
    private ProgressBar pbLoading;
    private View rootContainer;
    private View viewGreenOverlay;

    // Views Welcome
    private LinearLayout layoutBottom;
    private Button btnLogin;
    private TextView tvSkip;
    private TextView tvWelcomeMessage; // [MỚI] Khai báo TextView câu chào

    // State
    private boolean isLoggedIn = false;
    private String savedName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkLoginState();
        initViews();
        setupInitialState();
    }

    private void checkLoginState() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            isLoggedIn = true;
            savedName = tokenManager.getUsername();
        } else {
            isLoggedIn = false;
        }
    }

    private void initViews() {
        horizontalGroup = findViewById(R.id.horizontalGroup);
        ivLogoBero = findViewById(R.id.ivLogoBero);
        tvTravel = findViewById(R.id.tvTravel);
        tvSlogan = findViewById(R.id.tvSlogan);
        pbLoading = findViewById(R.id.pbLoading);
        rootContainer = findViewById(R.id.rootContainer);
        viewGreenOverlay = findViewById(R.id.viewGreenOverlay);

        layoutBottom = findViewById(R.id.layout_bottom);
        btnLogin = findViewById(R.id.btn_login);
        tvSkip = findViewById(R.id.tv_skip);

        // [MỚI] Ánh xạ TextView câu chào
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);

        tvSkip.setPaintFlags(tvSkip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void setupButtonActions() {
        if (isLoggedIn) {
            // Đã đăng nhập
            btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(SplashActivity.this, BaseActivity.class));
                finish();
            });

            tvSkip.setOnClickListener(v -> {
                TokenManager.getInstance(this).clearSession();
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);
            });

        } else {
            // Chưa đăng nhập
            btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            });

            tvSkip.setOnClickListener(v -> {
                startActivity(new Intent(SplashActivity.this, BaseActivity.class));
                finish();
            });
        }
    }

    private void setupInitialState() {
        // ... (Giữ nguyên phần animation setup ban đầu) ...
        horizontalGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                horizontalGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float travelWidth = tvTravel.getWidth() + ((LinearLayout.LayoutParams)tvTravel.getLayoutParams()).getMarginStart();
                float startOffsetY = 100f;
                horizontalGroup.setTranslationX(travelWidth / 2f);
                horizontalGroup.setTranslationY(startOffsetY);
                tvTravel.setTranslationX(-travelWidth);
                tvTravel.setAlpha(0f);
                tvTravel.setVisibility(View.VISIBLE);
                tvSlogan.setTranslationY(-30f);
                tvSlogan.setAlpha(0f);
                tvSlogan.setVisibility(View.VISIBLE);
                pbLoading.setAlpha(0f);
                pbLoading.setVisibility(View.VISIBLE);
                viewGreenOverlay.setAlpha(1f);
                runSplashAnimation();
            }
        });
    }

    private void runSplashAnimation() {
        // ... (Giữ nguyên logic animation chạy logo) ...
        AnimatorSet splashSet = new AnimatorSet();
        ObjectAnimator moveUp = ObjectAnimator.ofFloat(horizontalGroup, "translationY", 0f);
        moveUp.setDuration(800);
        moveUp.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator containerShiftLeft = ObjectAnimator.ofFloat(horizontalGroup, "translationX", 0f);
        ObjectAnimator textSlideOut = ObjectAnimator.ofFloat(tvTravel, "translationX", 0f);
        ObjectAnimator textFade = ObjectAnimator.ofFloat(tvTravel, "alpha", 1f);
        long slideDuration = 600;
        containerShiftLeft.setDuration(slideDuration);
        textSlideOut.setDuration(slideDuration);
        textFade.setDuration(slideDuration);
        containerShiftLeft.setInterpolator(new OvershootInterpolator(0.8f));
        textSlideOut.setInterpolator(new OvershootInterpolator(0.8f));
        AnimatorSet spreadSet = new AnimatorSet();
        spreadSet.playTogether(containerShiftLeft, textSlideOut, textFade);
        ObjectAnimator sloganDrop = ObjectAnimator.ofFloat(tvSlogan, "translationY", 0f);
        ObjectAnimator sloganFade = ObjectAnimator.ofFloat(tvSlogan, "alpha", 1f);
        sloganDrop.setDuration(800);
        sloganFade.setDuration(800);
        AnimatorSet sloganSet = new AnimatorSet();
        sloganSet.playTogether(sloganDrop, sloganFade);
        ObjectAnimator loadingFade = ObjectAnimator.ofFloat(pbLoading, "alpha", 1f);
        loadingFade.setDuration(500);
        splashSet.playSequentially(moveUp, spreadSet, sloganSet, loadingFade);
        splashSet.setStartDelay(200);
        splashSet.start();
        new Handler(Looper.getMainLooper()).postDelayed(this::transitionToWelcomeState, 5000);
    }

    private void transitionToWelcomeState() {
        // [CẬP NHẬT UI TEXT DỰA TRÊN TRẠNG THÁI LOGIN]
        if (isLoggedIn) {
            // 1. Cập nhật nút bấm
            btnLogin.setText("Tiếp tục dưới tên " + savedName);
            tvSkip.setText("Đăng xuất");

            // 2. [MỚI] Cập nhật câu chào mừng
            tvWelcomeMessage.setText("Chào mừng trở lại, " + savedName + "!");
        } else {
            // 1. Cập nhật nút bấm
            btnLogin.setText("Đăng nhập ngay");
            tvSkip.setText("Hoặc bỏ qua");

            // 2. [MỚI] Reset câu chào mặc định (đề phòng trường hợp logout xong load lại)
            tvWelcomeMessage.setText("Luôn đồng hành cùng hành trình của bạn!");
        }

        // Cập nhật lại Action cho nút bấm
        setupButtonActions();

        // --- Animation chuyển tiếp (Giữ nguyên) ---
        ObjectAnimator loadingFadeOut = ObjectAnimator.ofFloat(pbLoading, "alpha", 0f);
        loadingFadeOut.setDuration(500);

        ObjectAnimator overlayFadeOut = ObjectAnimator.ofFloat(viewGreenOverlay, "alpha", 0f);
        overlayFadeOut.setDuration(1000);

        ObjectAnimator logoMoveUp = ObjectAnimator.ofFloat(rootContainer, "translationY", -100f);
        logoMoveUp.setDuration(800);
        logoMoveUp.setInterpolator(new DecelerateInterpolator());

        layoutBottom.setTranslationY(layoutBottom.getHeight());
        layoutBottom.setAlpha(0f);
        layoutBottom.setVisibility(View.VISIBLE);

        ObjectAnimator bottomSheetSlideUp = ObjectAnimator.ofFloat(layoutBottom, "translationY", 0f);
        ObjectAnimator bottomSheetFadeIn = ObjectAnimator.ofFloat(layoutBottom, "alpha", 1f);
        bottomSheetSlideUp.setDuration(800);
        bottomSheetFadeIn.setDuration(800);
        bottomSheetSlideUp.setInterpolator(new DecelerateInterpolator());

        AnimatorSet transitionSet = new AnimatorSet();
        transitionSet.play(loadingFadeOut)
                .with(overlayFadeOut)
                .with(logoMoveUp)
                .with(bottomSheetSlideUp)
                .with(bottomSheetFadeIn);

        transitionSet.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            pbLoading.setVisibility(View.GONE);
            viewGreenOverlay.setVisibility(View.GONE);
        }, 1000);
    }
}