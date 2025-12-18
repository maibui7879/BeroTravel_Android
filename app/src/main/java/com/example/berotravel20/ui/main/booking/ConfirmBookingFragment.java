package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.ui.common.BaseFragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConfirmBookingFragment extends BaseFragment {
    private String placeId, placeName, placeAddress, placeImageUrl;
    private int pricePerDay, guests;
    private long checkinTime, checkoutTime;

    public static ConfirmBookingFragment newInstance(String id, String name, String addr, String img, int price, int g, long start, long end) {
        ConfirmBookingFragment f = new ConfirmBookingFragment();
        Bundle a = new Bundle();
        a.putString("KEY_PLACE_ID", id);
        a.putString("KEY_PLACE_NAME", name);
        a.putString("KEY_PLACE_ADDR", addr);
        a.putString("KEY_PLACE_IMG", img);
        a.putInt("KEY_PRICE_PER_DAY", price);
        a.putInt("KEY_GUESTS", g);
        a.putLong("KEY_CHECKIN", start);
        a.putLong("KEY_CHECKOUT", end);
        f.setArguments(a);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("KEY_PLACE_ID");
            placeName = getArguments().getString("KEY_PLACE_NAME");
            placeAddress = getArguments().getString("KEY_PLACE_ADDR");
            placeImageUrl = getArguments().getString("KEY_PLACE_IMG");
            pricePerDay = getArguments().getInt("KEY_PRICE_PER_DAY");
            guests = getArguments().getInt("KEY_GUESTS");
            checkinTime = getArguments().getLong("KEY_CHECKIN");
            checkoutTime = getArguments().getLong("KEY_CHECKOUT");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tính toán
        long diff = checkoutTime - checkinTime;
        long days = (long) Math.ceil(diff / (24.0 * 60 * 60 * 1000));
        if (days < 1) days = 1;

        double subtotal = (double) pricePerDay * days * guests;
        double tax = subtotal * 0.05;
        double grandTotal = subtotal + tax;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // 2. Hiển thị (Khắc phục lỗi hiển thị 0 trong ảnh của bạn)
        ((TextView) view.findViewById(R.id.tv_place_name)).setText(placeName);
        ((TextView) view.findViewById(R.id.tv_place_address)).setText(placeAddress);
        ((TextView) view.findViewById(R.id.tv_price_per_day)).setText(formatVND(pricePerDay));
        ((TextView) view.findViewById(R.id.tv_guests)).setText(guests + " người");
        ((TextView) view.findViewById(R.id.tv_total_days)).setText(days + " ngày/đêm");
        ((TextView) view.findViewById(R.id.tv_dates)).setText(sdf.format(new Date(checkinTime)) + " - " + sdf.format(new Date(checkoutTime)));
        ((TextView) view.findViewById(R.id.tv_tax)).setText(formatVND(tax));
        ((TextView) view.findViewById(R.id.tv_total_price)).setText(formatVND(grandTotal));

        // Ngày giờ chi tiết ở dưới
        ((TextView) view.findViewById(R.id.tv_checkin_date)).setText(sdf.format(new Date(checkinTime)));
        ((TextView) view.findViewById(R.id.tv_checkout_date)).setText(sdf.format(new Date(checkoutTime)));

        Glide.with(this).load(placeImageUrl).into((ImageView) view.findViewById(R.id.img_place));

        view.findViewById(R.id.btn_proceed_payment).setOnClickListener(v -> {
            ReviewReservationFragment review = ReviewReservationFragment.newInstance(
                    placeId, placeName, placeAddress, placeImageUrl, pricePerDay, guests, checkinTime, checkoutTime
            );
            replaceFragment(review);
        });

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBack());
    }

    private String formatVND(double amount) {
        return String.format(Locale.getDefault(), "%,.0f đ", amount);
    }
}