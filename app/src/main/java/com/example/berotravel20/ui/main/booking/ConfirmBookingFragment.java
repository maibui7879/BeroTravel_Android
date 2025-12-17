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

    public static ConfirmBookingFragment newInstance(String id, String name, String addr, String img, int price, int guests, long start, long end) {
        ConfirmBookingFragment f = new ConfirmBookingFragment();
        Bundle a = new Bundle();
        a.putString("id", id); a.putString("name", name); a.putString("addr", addr);
        a.putString("img", img); a.putInt("price", price); a.putInt("guests", guests);
        a.putLong("start", start); a.putLong("end", end);
        f.setArguments(a);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("id");
            placeName = getArguments().getString("name");
            placeAddress = getArguments().getString("addr");
            placeImageUrl = getArguments().getString("img");
            pricePerDay = getArguments().getInt("price");
            guests = getArguments().getInt("guests");
            checkinTime = getArguments().getLong("start");
            checkoutTime = getArguments().getLong("end");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Logic tính toán
        long diff = checkoutTime - checkinTime;
        final long finalDays = (diff / (24 * 60 * 60 * 1000) < 1) ? 1 : diff / (24 * 60 * 60 * 1000);
        double subtotal = (double) pricePerDay * finalDays * guests;
        double serviceFee = subtotal * 0.05;
        final double grandTotal = subtotal + serviceFee;

        // Định dạng ngày
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Định dạng đầy đủ

        final String datesDisplay = sdf.format(new Date(checkinTime)) + " - " + sdf.format(new Date(checkoutTime));

        // 2. Hiển thị dữ liệu
        ((TextView) view.findViewById(R.id.tv_place_name)).setText(placeName);
        ((TextView) view.findViewById(R.id.tv_place_address)).setText(placeAddress);
        ((TextView) view.findViewById(R.id.tv_dates)).setText(datesDisplay);
        ((TextView) view.findViewById(R.id.tv_total_days)).setText(finalDays + " đêm");
        ((TextView) view.findViewById(R.id.tv_price_per_day)).setText(formatVND(pricePerDay));
        ((TextView) view.findViewById(R.id.tv_tax)).setText(formatVND(serviceFee));
        ((TextView) view.findViewById(R.id.tv_total_price)).setText(formatVND(grandTotal));
        ((TextView) view.findViewById(R.id.tv_guests)).setText(guests + " người");

        // SỬA LỖI --/--/-- TẠI ĐÂY
        ((TextView) view.findViewById(R.id.tv_checkin_date)).setText(sdfFull.format(new Date(checkinTime)));
        ((TextView) view.findViewById(R.id.tv_checkout_date)).setText(sdfFull.format(new Date(checkoutTime)));

        Glide.with(this).load(placeImageUrl).into((ImageView) view.findViewById(R.id.img_place));

        // 3. Nút tiếp tục
        view.findViewById(R.id.btn_proceed_payment).setOnClickListener(v -> {
            ReviewReservationFragment review = ReviewReservationFragment.newInstance(
                    placeId, placeName, placeAddress, placeImageUrl,
                    pricePerDay, guests, checkinTime, checkoutTime
            );
            replaceFragment(review);
        });

        // 4. Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private String formatVND(double amount) {
        return String.format(Locale.getDefault(), "%,.0f đ", amount);
    }
}