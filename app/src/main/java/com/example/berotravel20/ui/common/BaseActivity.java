package com.example.berotravel20.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.berotravel20.R;
import com.example.berotravel20.ui.main.home.HomeFragment;

import com.example.berotravel20.ui.main.place.PlaceFragment;
import com.example.berotravel20.ui.map.AddPlaceFragment;
import com.example.berotravel20.ui.map.MapActivity;
import com.example.berotravel20.ui.main.profile.AccountFragment;
import com.example.berotravel20.ui.main.journey.JourneyFragment;
import com.example.berotravel20.ui.main.booking.BookingHistory;
import com.example.berotravel20.ui.main.notification.NotificationFragment;

public class BaseActivity extends AppCompatActivity {

    private LinearLayout navHome, navMap, navItinerary, navBooking, navNotifications, navAccount;
    private ImageView iconHome, iconMap, iconItinerary, iconBooking, iconNotifications, iconAccount;
    private TextView textHome, textMap, textItinerary, textBooking, textNotifications, textAccount;
    private NestedScrollView mainScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        initViews();
        setupClickListeners();
        mainScrollView = findViewById(R.id.main_scroll_view);
        // Xử lý Intent lúc khởi tạo (nếu có)
        if (handleIntent(getIntent())) {
            resetTabColors();
        } else {
            setSelected(navHome);
            replaceFragment(new HomeFragment());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật Intent mới nhất để handleIntent lấy được dữ liệu [Cực kỳ quan trọng]
        handleIntent(intent);
    }
    public void resetMainScroll() {
        if (mainScrollView != null) {
            mainScrollView.scrollTo(0, 0);
            // Hoặc dùng: mainScrollView.fullScroll(View.FOCUS_UP);
        }
    }
    private boolean handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("NAVIGATE_TO")) {
            String navigateTo = intent.getStringExtra("NAVIGATE_TO");

            // Logic điều hướng về Booking History
            if ("BOOKING_HISTORY".equals(navigateTo)) {
                setSelected(navBooking);
                replaceFragment(new BookingHistory());
                return true;
            }
            if ("ADD_PLACE".equals(navigateTo)) {
                double lat = intent.getDoubleExtra("LAT", 0);
                double lng = intent.getDoubleExtra("LNG", 0);
                // Mở AddPlaceFragment
                replaceFragment(AddPlaceFragment.newInstance(lat, lng));
                return true;
            }
            // Logic xem chi tiết địa điểm
            if ("PLACE_DETAIL".equals(navigateTo)) {
                String placeId = intent.getStringExtra("PLACE_ID");
                if (placeId != null) {
                    openPlaceDetailFragment(placeId);
                    return true;
                }
            }
        }
        return false;
    }

    private void openPlaceDetailFragment(String placeId) {
        PlaceFragment fragment = PlaceFragment.newInstance(placeId);
        navigateToDetail(fragment);
    }

    private void initViews() {
        navHome = findViewById(R.id.nav_home);
        navMap = findViewById(R.id.nav_map);
        navItinerary = findViewById(R.id.nav_itinerary);
        navBooking = findViewById(R.id.nav_booking);
        navNotifications = findViewById(R.id.nav_notifications);
        navAccount = findViewById(R.id.nav_account);

        iconHome = navHome.findViewById(R.id.icon_home);
        textHome = navHome.findViewById(R.id.text_home);
        iconMap = navMap.findViewById(R.id.icon_map);
        textMap = navMap.findViewById(R.id.text_map);
        iconItinerary = navItinerary.findViewById(R.id.icon_itinerary);
        textItinerary = navItinerary.findViewById(R.id.text_itinerary);
        iconBooking = navBooking.findViewById(R.id.icon_booking);
        textBooking = navBooking.findViewById(R.id.text_booking);
        iconNotifications = navNotifications.findViewById(R.id.icon_notifications);
        textNotifications = navNotifications.findViewById(R.id.text_notifications);
        iconAccount = navAccount.findViewById(R.id.icon_account);
        textAccount = navAccount.findViewById(R.id.text_account);
    }

    private void setupClickListeners() {
        navHome.setOnClickListener(v -> { setSelected(navHome); replaceFragment(new HomeFragment()); });
        navMap.setOnClickListener(v -> { startActivity(new Intent(BaseActivity.this, MapActivity.class)); });
        navItinerary.setOnClickListener(v -> { setSelected(navItinerary); replaceFragment(new JourneyFragment()); });
        navBooking.setOnClickListener(v -> { setSelected(navBooking); replaceFragment(new BookingHistory()); });
        navNotifications.setOnClickListener(v -> { setSelected(navNotifications); replaceFragment(new NotificationFragment()); });
        navAccount.setOnClickListener(v -> { setSelected(navAccount); replaceFragment(new AccountFragment()); });
    }

    private void setSelected(View selected) {
        resetTabColors();
        if (selected == navHome) setActive(iconHome, textHome);
        else if (selected == navItinerary) setActive(iconItinerary, textItinerary);
        else if (selected == navBooking) setActive(iconBooking, textBooking);
        else if (selected == navNotifications) setActive(iconNotifications, textNotifications);
        else if (selected == navAccount) setActive(iconAccount, textAccount);
    }

    private void resetTabColors() {
        resetTab(iconHome, textHome); resetTab(iconMap, textMap);
        resetTab(iconItinerary, textItinerary); resetTab(iconBooking, textBooking);
        resetTab(iconNotifications, textNotifications); resetTab(iconAccount, textAccount);
    }

    private void resetTab(ImageView icon, TextView label) {
        if (icon != null) icon.setColorFilter(getColor(R.color.nav_default));
        if (label != null) label.setTextColor(getColor(R.color.nav_default));
    }

    private void setActive(ImageView icon, TextView label) {
        if (icon != null) icon.setColorFilter(getColor(R.color.teal_700));
        if (label != null) label.setTextColor(getColor(R.color.teal_700));
    }

    public void replaceFragment(Fragment fragment) {
        resetMainScroll();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        String tag = fragment.getClass().getSimpleName();
        getSupportFragmentManager().beginTransaction().replace(R.id.base_container, fragment, tag).commit();
    }

    public void navigateToDetail(Fragment fragment) {
        resetMainScroll();
        String tag = fragment.getClass().getSimpleName();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.base_container, fragment)
                .addToBackStack(tag).commit();
    }

    public void showLoading() {
        View pb = findViewById(R.id.pb_loading);
        if (pb != null) pb.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        View pb = findViewById(R.id.pb_loading);
        if (pb != null) pb.setVisibility(View.GONE);
    }
}