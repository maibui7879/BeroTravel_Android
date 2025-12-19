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

import androidx.appcompat.app.AppCompatActivity;
import com.example.berotravel20.R;
import com.example.berotravel20.ui.auth.LoginActivity;
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.ui.main.MainActivity;

import com.example.berotravel20.data.local.TokenManager;
// Removed unused AuthActivity import

public class SplashActivity extends AppCompatActivity {

    // Views Splash
    private LinearLayout horizontalGroup;
    private View ivLogoBero;
    private TextView tvTravel;
    private TextView tvSlogan;
    private ProgressBar pbLoading;
    private View rootContainer;
    private View viewGreenOverlay; // [MỚI] View nền xanh

    // Views Welcome
    private LinearLayout layoutBottom;
    private Button btnLogin;
    private TextView tvSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Bind Views
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

        // Check Token
        new Handler().postDelayed(() -> {
            TokenManager tokenManager = TokenManager.getInstance(this);
            String token = tokenManager.getToken();
            if (token != null && !token.isEmpty()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            } else {
                setupInitialState();
                setupWelcomeListeners();
            }
        }, 500);
    }

    private void setupWelcomeListeners() {
        btnLogin.setOnClickListener(v -> startActivity(new Intent(SplashActivity.this, LoginActivity.class)));
        tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, BaseActivity.class));
            finish();
        });
    }

    private void setupInitialState() {
        horizontalGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                horizontalGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float travelWidth = tvTravel.getWidth()
                        + ((LinearLayout.LayoutParams) tvTravel.getLayoutParams()).getMarginStart();
                float startOffsetY = 100f;

                // Set vị trí ban đầu
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

                // Đảm bảo nền xanh đang che kín (alpha = 1)
                viewGreenOverlay.setAlpha(1f);

                runSplashAnimation();
            }
        });
    }

    private void runSplashAnimation() {
        // --- 1. ANIMATION CỦA LOGO (TRÊN NỀN XANH) ---
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

        // Đợi 5s rồi chuyển trạng thái
        new Handler(Looper.getMainLooper()).postDelayed(this::transitionToWelcomeState, 5000);
    }

    private void transitionToWelcomeState() {
        // --- 2. ANIMATION CHUYỂN TIẾP (HIỆN ẢNH NỀN) ---

        // A. Làm mờ Loading
        ObjectAnimator loadingFadeOut = ObjectAnimator.ofFloat(pbLoading, "alpha", 0f);
        loadingFadeOut.setDuration(500);

        // B. Làm mờ lớp phủ màu xanh -> Lộ ảnh nền ra
        ObjectAnimator overlayFadeOut = ObjectAnimator.ofFloat(viewGreenOverlay, "alpha", 0f);
        overlayFadeOut.setDuration(1000); // Mất 1 giây để chuyển từ Xanh -> Ảnh

        // C. Đẩy Logo lên một chút
        ObjectAnimator logoMoveUp = ObjectAnimator.ofFloat(rootContainer, "translationY", -100f);
        logoMoveUp.setDuration(800);
        logoMoveUp.setInterpolator(new DecelerateInterpolator());

        // D. Hiện Bottom Sheet từ dưới lên
        layoutBottom.setVisibility(View.VISIBLE);
        layoutBottom.setTranslationY(layoutBottom.getHeight());

        ObjectAnimator bottomSheetSlideUp = ObjectAnimator.ofFloat(layoutBottom, "translationY", 0f);
        ObjectAnimator bottomSheetFadeIn = ObjectAnimator.ofFloat(layoutBottom, "alpha", 1f);

        bottomSheetSlideUp.setDuration(800);
        bottomSheetFadeIn.setDuration(800);
        bottomSheetSlideUp.setInterpolator(new DecelerateInterpolator());

        // GỘP TẤT CẢ VÀO ANIMATOR SET
        AnimatorSet transitionSet = new AnimatorSet();

        // Chạy cùng lúc: Ẩn loading, Mất nền xanh, Logo lên, Khung dưới hiện ra
        transitionSet.play(loadingFadeOut)
                .with(overlayFadeOut)
                .with(logoMoveUp)
                .with(bottomSheetSlideUp)
                .with(bottomSheetFadeIn);

        transitionSet.start();

        // Dọn dẹp View không cần thiết
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            pbLoading.setVisibility(View.GONE);
            viewGreenOverlay.setVisibility(View.GONE); // Ẩn hẳn view xanh đi cho nhẹ
        }, 1000);
    }
}