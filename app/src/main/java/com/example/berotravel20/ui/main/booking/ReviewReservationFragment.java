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

public class ReviewReservationFragment extends BaseFragment {
    private String placeId, placeName, placeAddress, placeImageUrl;
    private int guests, pricePerDay;
    private long checkinMillis, checkoutMillis;

    public static ReviewReservationFragment newInstance(String id, String name, String addr, String img, int price, int g, long start, long end) {
        ReviewReservationFragment f = new ReviewReservationFragment();
        Bundle a = new Bundle();
        a.putString("id", id); a.putString("name", name); a.putString("addr", addr);
        a.putString("img", img); a.putInt("price", price); a.putInt("g", g);
        a.putLong("s", start); a.putLong("e", end);
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
            guests = getArguments().getInt("g");
            checkinMillis = getArguments().getLong("s");
            checkoutMillis = getArguments().getLong("e");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar(view);
        displayData(view);

        // Chuyển sang màn hình QR Payment thay vì gọi API ngay
        view.findViewById(R.id.btn_book_now).setOnClickListener(v -> navigateToPayment());
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }
    }

    private void displayData(View view) {
        ((TextView) view.findViewById(R.id.tv_place_name_review)).setText(placeName);
        ((TextView) view.findViewById(R.id.tv_place_address_review)).setText(placeAddress);
        Glide.with(this).load(placeImageUrl).placeholder(R.drawable.placeholder_image).into((ImageView) view.findViewById(R.id.img_place_review));

        long diff = checkoutMillis - checkinMillis;
        long nights = (diff / (24 * 60 * 60 * 1000) < 1) ? 1 : diff / (24 * 60 * 60 * 1000);

        SimpleDateFormat sdfFull = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat sdfShort = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Đổ dữ liệu vào 2 ô nhận/trả phòng (Sửa lỗi --/--/--)
        TextView tvIn = view.findViewById(R.id.tv_checkin_review);
        TextView tvOut = view.findViewById(R.id.tv_checkout_review);
        if (tvIn != null) tvIn.setText(sdfFull.format(new Date(checkinMillis)));
        if (tvOut != null) tvOut.setText(sdfFull.format(new Date(checkoutMillis)));

        String summary = sdfShort.format(new Date(checkinMillis)) + " - " + sdfShort.format(new Date(checkoutMillis)) + " (" + nights + " đêm)";
        ((TextView) view.findViewById(R.id.tv_stay_dates)).setText(summary);
        ((TextView) view.findViewById(R.id.tv_stay_guests)).setText(guests + " khách");

        double total = (pricePerDay * nights) * 1.05;
        ((TextView) view.findViewById(R.id.tv_total_review)).setText(String.format(Locale.getDefault(), "%,.0f đ", total));
    }

    private void navigateToPayment() {
        long diff = checkoutMillis - checkinMillis;
        long nights = (diff / (24 * 60 * 60 * 1000) < 1) ? 1 : diff / (24 * 60 * 60 * 1000);
        double total = (pricePerDay * nights) * 1.05;

        // Chuyển sang PaymentQrFragment
        PaymentQrFragment qrFragment = PaymentQrFragment.newInstance(
                placeId, placeName, guests, String.format(Locale.getDefault(), "%,.0f đ", total),
                checkinMillis, checkoutMillis
        );
        replaceFragment(qrFragment);
    }
}