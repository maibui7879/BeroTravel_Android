package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.util.Log;
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
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Booking.BookingCreateResponse;
import com.example.berotravel20.data.repository.BookingRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReviewReservationFragment extends BaseFragment {
    private static final String TAG = "API_CREATE_BOOKING";

    // Khai báo các biến nhận từ Fragment trước
    private String placeId, placeName, placeAddress, placeImageUrl;
    private int guests, pricePerDay;
    private long checkinMillis, checkoutMillis;

    public static ReviewReservationFragment newInstance(String id, String name, String addr, String img, int price, int g, long start, long end) {
        ReviewReservationFragment f = new ReviewReservationFragment();
        Bundle a = new Bundle();
        a.putString("placeId", id);
        a.putString("placeName", name);
        a.putString("placeAddress", addr);
        a.putString("placeImageUrl", img);
        a.putInt("pricePerDay", price);
        a.putInt("guests", g);
        a.putLong("checkinMillis", start);
        a.putLong("checkoutMillis", end);
        f.setArguments(a);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("placeId");
            placeName = getArguments().getString("placeName");
            placeAddress = getArguments().getString("placeAddress");
            placeImageUrl = getArguments().getString("placeImageUrl");
            pricePerDay = getArguments().getInt("pricePerDay");
            guests = getArguments().getInt("guests");
            checkinMillis = getArguments().getLong("checkinMillis");
            checkoutMillis = getArguments().getLong("checkoutMillis");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Sử dụng file XML bạn vừa cung cấp
        return inflater.inflate(R.layout.fragment_review_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayData(view);
        setupToolbar(view);

        // Nút Xác nhận đặt phòng
        view.findViewById(R.id.btn_book_now).setOnClickListener(v -> createBookingToServer());
    }

    private void displayData(View view) {
        // 1. Ánh xạ và hiển thị thông tin địa điểm
        ((TextView) view.findViewById(R.id.tv_place_name_review)).setText(placeName);
        ((TextView) view.findViewById(R.id.tv_place_address_review)).setText(placeAddress);
        Glide.with(this)
                .load(placeImageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into((ImageView) view.findViewById(R.id.img_place_review));

        // 2. Định dạng và hiển thị Ngày nhận/trả phòng (KHẮC PHỤC LỖI HIỂN THỊ)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        TextView tvCheckin = view.findViewById(R.id.tv_checkin_review);
        TextView tvCheckout = view.findViewById(R.id.tv_checkout_review);

        if (tvCheckin != null) tvCheckin.setText(sdf.format(new Date(checkinMillis)));
        if (tvCheckout != null) tvCheckout.setText(sdf.format(new Date(checkoutMillis)));

        // 3. Hiển thị số lượng khách
        TextView tvGuests = view.findViewById(R.id.tv_stay_guests);
        if (tvGuests != null) tvGuests.setText(guests + " khách");

        // 4. Tính toán số đêm và Tổng thanh toán (Tổng = Giá * Đêm * Người + 5% Thuế)
        long diff = checkoutMillis - checkinMillis;
        long nights = (long) Math.ceil(diff / (24.0 * 60 * 60 * 1000));
        if (nights < 1) nights = 1;

        double subtotal = (double) pricePerDay * nights * guests;
        double grandTotal = subtotal * 1.05; // Thuế phí 5%

        TextView tvTotal = view.findViewById(R.id.tv_total_review);
        if (tvTotal != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", grandTotal));
        }
    }

    private void createBookingToServer() {
        showLoading();
        new BookingRepository().createBooking(placeId, guests, checkinMillis, checkoutMillis, new DataCallback<BookingCreateResponse>() {
            @Override
            public void onSuccess(BookingCreateResponse result) {
                hideLoading();
                if (isAdded() && result != null) {
                    navigateToPayment(result.id);
                }
            }

            @Override
            public void onError(String msg) {
                hideLoading();
                if (isAdded()) showError("Lỗi: " + msg);
            }
        });
    }

    private void navigateToPayment(String bookingId) {
        // Tính lại tổng tiền để truyền sang trang QR
        long diff = checkoutMillis - checkinMillis;
        long nights = (long) Math.ceil(diff / (24.0 * 60 * 60 * 1000));
        if (nights < 1) nights = 1;
        double total = (pricePerDay * nights * guests) * 1.05;

        PaymentQrFragment qrFragment = PaymentQrFragment.newInstance(
                bookingId,
                placeName,
                String.format(Locale.getDefault(), "%,.0f đ", total)
        );
        replaceFragment(qrFragment);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBack());
        }
    }
}