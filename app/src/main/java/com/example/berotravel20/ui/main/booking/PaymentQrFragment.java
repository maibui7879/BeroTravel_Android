package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.berotravel20.R;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.repository.BookingRepository;
import com.example.berotravel20.ui.common.BaseFragment;

public class PaymentQrFragment extends BaseFragment {
    private String placeId, placeName, totalStr;
    private int guests;
    private long checkin, checkout;

    public static PaymentQrFragment newInstance(String id, String name, int g, String total, long start, long end) {
        PaymentQrFragment f = new PaymentQrFragment();
        Bundle b = new Bundle();
        b.putString("id", id); b.putString("name", name); b.putInt("g", g);
        b.putString("total", total); b.putLong("s", start); b.putLong("e", end);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("id");
            placeName = getArguments().getString("name");
            guests = getArguments().getInt("g");
            totalStr = getArguments().getString("total");
            checkin = getArguments().getLong("s");
            checkout = getArguments().getLong("e");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Giả lập: Sau 3 giây QR quét thành công -> Thực hiện lưu vào DB
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                saveBookingToDatabase();
            }
        }, 3000);
    }

    private void saveBookingToDatabase() {
        showLoading();
        BookingRepository repo = new BookingRepository();
        repo.createBooking(placeId, guests, checkin, checkout, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                // Chỉ chuyển sang Success khi Database đã lưu xong
                replaceFragment(new BookingSuccessFragment());
            }

            @Override
            public void onError(String msg) {
                hideLoading();
                showError("Lỗi hệ thống: " + msg);
            }
        });
    }
}