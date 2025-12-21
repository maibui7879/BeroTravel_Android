package com.example.berotravel20.ui.main.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.berotravel20.R;
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.ui.common.BaseFragment;

public class BookingSuccessFragment extends BaseFragment {
    private String placeId, placeName, placeAddr, placeImg;
    private int guests, pricePerDay;
    private long start, end;

    public static BookingSuccessFragment newInstance(String id, String name, String addr, String img, int price, int g, long start, long end) {
        BookingSuccessFragment f = new BookingSuccessFragment();
        Bundle b = new Bundle();
        b.putString("id", id); b.putString("name", name); b.putString("addr", addr);
        b.putString("img", img); b.putInt("price", price); b.putInt("g", g);
        b.putLong("s", start); b.putLong("e", end);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("id"); placeName = getArguments().getString("name");
            placeAddr = getArguments().getString("addr"); placeImg = getArguments().getString("img");
            pricePerDay = getArguments().getInt("price"); guests = getArguments().getInt("g");
            start = getArguments().getLong("s"); end = getArguments().getLong("e");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tạo mã đặt chỗ ngẫu nhiên
        String bookingId = "BERO-" + (System.currentTimeMillis() / 100000);
        ((TextView)view.findViewById(R.id.tv_booking_id)).setText("Mã đặt chỗ: " + bookingId);

        // Hiển thị email người dùng
        if (tokenManager != null && tokenManager.getUsername() != null) {
            ((TextView)view.findViewById(R.id.tv_user_email)).setText(tokenManager.getUsername());
        }

        // Logic quay về trang chủ (Home)
        view.findViewById(R.id.btn_back_to_home).setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtra("NAVIGATE_TO", "HOME");
                startActivity(intent);
            }
        });
    }
}