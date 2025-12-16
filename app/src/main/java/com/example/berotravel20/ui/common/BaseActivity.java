package com.example.berotravel20.ui.common;

import android.content.Intent; // Nhớ import Intent
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;
import com.example.berotravel20.ui.main.ai.AIFragment;
import com.example.berotravel20.ui.main.home.HomeFragment;
import com.example.berotravel20.ui.map.MapActivity; // Import đúng MapActivity
import com.example.berotravel20.ui.main.profile.AccountFragment;
import com.example.berotravel20.ui.main.setting.SettingFragment;
import com.example.berotravel20.ui.main.journey.JourneyFragment;

public class BaseActivity extends AppCompatActivity {

    LinearLayout navHome, navMap, navAI, navSetting, navAccount, navJourney;
    ImageView iconHome, iconMap, iconAI, iconSetting, iconAccount, iconJourney;
    TextView textHome, textMap, textAI, textSetting, textAccount, textJourney;
    View bottomNavbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        initViews();
        setupClickListeners();

        // Load fragment mặc định là Home
        setSelected(navHome);
        replaceFragment(new HomeFragment());

        // Handle Bottom Navigation Visibility
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                bottomNavbar.setVisibility(View.GONE);
            } else {
                bottomNavbar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews() {
        bottomNavbar = findViewById(R.id.bottom_navbar); // Main container

        navHome = findViewById(R.id.nav_home);
        navMap = findViewById(R.id.nav_map);
        navAI = findViewById(R.id.nav_ai);
        navSetting = findViewById(R.id.nav_setting);
        navAccount = findViewById(R.id.nav_account);

        iconHome = findViewById(R.id.icon_home);
        iconMap = findViewById(R.id.icon_map);
        iconAI = findViewById(R.id.icon_ai);
        iconSetting = findViewById(R.id.icon_setting);
        iconAccount = findViewById(R.id.icon_account);

        textHome = findViewById(R.id.text_home);
        textMap = findViewById(R.id.text_map);
        textAI = findViewById(R.id.text_ai);
        textSetting = findViewById(R.id.text_setting);
        textAccount = findViewById(R.id.text_account);

        navJourney = findViewById(R.id.nav_journey);
        iconJourney = findViewById(R.id.icon_journey);
        textJourney = findViewById(R.id.text_journey);
    }

    private void setupClickListeners() {

        navHome.setOnClickListener(v -> {
            setSelected(navHome);
            replaceFragment(new HomeFragment());
        });

        // --- SỬA ĐỔI Ở ĐÂY ---
        navMap.setOnClickListener(v -> {
            // Không gọi setSelected(navMap) vì ta sẽ rời khỏi màn hình này
            // Không gọi replaceFragment

            // Chuyển sang MapActivity riêng biệt
            Intent intent = new Intent(BaseActivity.this, MapActivity.class);
            startActivity(intent);
        });
        // ---------------------

        navAI.setOnClickListener(v -> {
            setSelected(navAI);
            replaceFragment(new AIFragment());
        });

        navSetting.setOnClickListener(v -> {
            setSelected(navSetting);
            replaceFragment(new SettingFragment());
        });

        navAccount.setOnClickListener(v -> {
            setSelected(navAccount);
            replaceFragment(new AccountFragment());
        });

        navJourney.setOnClickListener(v -> {
            setSelected(navJourney);
            replaceFragment(new JourneyFragment());
        });
    }

    /** Đổi màu icon & text khi chọn tab */
    private void setSelected(View selected) {
        // reset tất cả về màu mặc định
        resetTab(iconHome, textHome);
        resetTab(iconMap, textMap);
        resetTab(iconAI, textAI);
        resetTab(iconSetting, textSetting);
        resetTab(iconAccount, textAccount);
        resetTab(iconJourney, textJourney);

        // set màu active cho tab được chọn
        if (selected == navHome)
            setActive(iconHome, textHome);
        // if (selected == navMap) setActive(iconMap, textMap); // Map là Activity riêng
        // nên ko cần set active ở đây
        if (selected == navAI)
            setActive(iconAI, textAI);
        if (selected == navSetting)
            setActive(iconSetting, textSetting);
        if (selected == navAccount)
            setActive(iconAccount, textAccount);
        if (selected == navJourney)
            setActive(iconJourney, textJourney);
    }

    private void resetTab(ImageView icon, TextView label) {
        // Đảm bảo bạn có định nghĩa màu trong colors.xml hoặc dùng
        // Color.GRAY/Color.BLACK
        icon.setColorFilter(getColor(R.color.nav_default));
        label.setTextColor(getColor(R.color.nav_default));
    }

    private void setActive(ImageView icon, TextView label) {
        icon.setColorFilter(getColor(R.color.teal_700));
        label.setTextColor(getColor(R.color.teal_700));
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.base_container, fragment)
                .commit();
    }
}