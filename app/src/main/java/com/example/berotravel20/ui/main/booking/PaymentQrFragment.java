package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;

public class PaymentQrFragment extends Fragment {

    // Same args as BookingSuccess to pass through
    private static final String ARG_PLACE_ID = "place_id";
    private static final String ARG_PLACE_NAME = "place_name";
    private static final String ARG_DATES = "place_dates";
    private static final String ARG_GUESTS = "place_guests";
    private static final String ARG_TOTAL = "place_total";
    private static final String ARG_CHECKIN_MILLIS = "checkin_millis";
    private static final String ARG_CHECKOUT_MILLIS = "checkout_millis";

    private String placeId, placeName, datesStr, totalStr;
    private int guests;
    private long checkinMillis, checkoutMillis;

    public static PaymentQrFragment newInstance(String placeId, String placeName, String dates, int guests,
            String total, long checkinMillis, long checkoutMillis) {
        PaymentQrFragment fragment = new PaymentQrFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE_ID, placeId);
        args.putString(ARG_PLACE_NAME, placeName);
        args.putString(ARG_DATES, dates);
        args.putInt(ARG_GUESTS, guests);
        args.putString(ARG_TOTAL, total);
        args.putLong(ARG_CHECKIN_MILLIS, checkinMillis);
        args.putLong(ARG_CHECKOUT_MILLIS, checkoutMillis);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString(ARG_PLACE_ID);
            placeName = getArguments().getString(ARG_PLACE_NAME);
            datesStr = getArguments().getString(ARG_DATES);
            guests = getArguments().getInt(ARG_GUESTS);
            totalStr = getArguments().getString(ARG_TOTAL);
            checkinMillis = getArguments().getLong(ARG_CHECKIN_MILLIS);
            checkoutMillis = getArguments().getLong(ARG_CHECKOUT_MILLIS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Auto move to Success after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                navigateToSuccess();
            }
        }, 2000); // 2 seconds delay
    }

    private void navigateToSuccess() {
        BookingSuccessFragment successFragment = BookingSuccessFragment.newInstance(
                placeId,
                placeName,
                datesStr,
                guests,
                totalStr,
                checkinMillis,
                checkoutMillis);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.base_container, successFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
