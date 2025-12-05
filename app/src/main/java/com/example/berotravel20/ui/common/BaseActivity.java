package com.example.berotravel20.ui.common;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;
import com.example.berotravel20.ui.main.ai.AIFragment;
import com.example.berotravel20.ui.main.home.HomeFragment;
import com.example.berotravel20.ui.main.map.MapFragment;
import com.example.berotravel20.ui.main.profile.AccountFragment;
import com.example.berotravel20.ui.main.setting.SettingFragment;
import com.google.android.gms.maps.SupportMapFragment;


public class BaseActivity extends AppCompatActivity {

    LinearLayout navHome, navMap, navAI, navSetting, navAccount;
    ImageView iconHome, iconMap, iconAI, iconSetting, iconAccount;
    TextView textHome, textMap, textAI, textSetting, textAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        initViews();
        setupClickListeners();

        // Load fragment mặc định
        setSelected(navHome);
        replaceFragment(new HomeFragment());
    }

    private void initViews() {
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
    }

    private void setupClickListeners() {

        navHome.setOnClickListener(v -> {
            setSelected(navHome);
            replaceFragment(new HomeFragment());
        });

        navMap.setOnClickListener(v -> {
            setSelected(navMap);
            replaceFragment(new MapFragment());
        });

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
    }

    /** Đổi màu icon & text khi chọn tab */
    private void setSelected(View selected) {

        // reset tất cả
        resetTab(iconHome, textHome);
        resetTab(iconMap, textMap);
        resetTab(iconAI, textAI);
        resetTab(iconSetting, textSetting);
        resetTab(iconAccount, textAccount);

        // set màu được chọn
        if (selected == navHome) setActive(iconHome, textHome);
        if (selected == navMap) setActive(iconMap, textMap);
        if (selected == navAI) setActive(iconAI, textAI);
        if (selected == navSetting) setActive(iconSetting, textSetting);
        if (selected == navAccount) setActive(iconAccount, textAccount);
    }

    private void resetTab(ImageView icon, TextView label) {
        icon.setColorFilter(getColor(R.color.nav_default));
        label.setTextColor(getColor(R.color.nav_default));
    }

    private void setActive(ImageView icon, TextView label) {
        icon.setColorFilter(getColor(R.color.teal_700)); // đổi màu icon khi chọn
        label.setTextColor(getColor(R.color.teal_700));  // đổi màu text khi chọn
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.base_container, fragment)
                .commit();
    }
}
