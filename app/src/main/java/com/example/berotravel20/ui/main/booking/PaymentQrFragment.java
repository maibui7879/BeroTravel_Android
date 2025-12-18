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
    private String bookingId, placeName, totalStr;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable payTask = this::executePayment;

    public static PaymentQrFragment newInstance(String bId, String name, String total) {
        PaymentQrFragment f = new PaymentQrFragment();
        Bundle b = new Bundle();
        b.putString("bookingId", bId);
        b.putString("placeName", name);
        b.putString("totalStr", total);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingId = getArguments().getString("bookingId");
            placeName = getArguments().getString("placeName");
            totalStr = getArguments().getString("totalStr");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bắt đầu đếm ngược 3 giây
        handler.postDelayed(payTask, 3000);
    }

    private void executePayment() {
        if (!isAdded()) return;

        showLoading();
        new BookingRepository().payBooking(bookingId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                replaceFragment(new BookingSuccessFragment());
            }

            @Override
            public void onError(String msg) {
                hideLoading();
                showError("Thanh toán lỗi: " + msg);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // QUAN TRỌNG: Hủy tiến trình 3 giây nếu người dùng nhấn thoát (Back)
        handler.removeCallbacks(payTask);
    }
}