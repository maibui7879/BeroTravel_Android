package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.models.BookingRequest;
import com.example.berotravel20.network.ApiClient;
import com.example.berotravel20.network.ApiService;
import com.example.berotravel20.ui.common.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmBookingFragment extends Fragment {

    private static final String ARG_PLACE_ID = "place_id";
    private static final String ARG_PLACE_NAME = "place_name";
    private static final String ARG_PLACE_ADDRESS = "place_address";
    private static final String ARG_PLACE_IMAGE = "place_image";
    private static final String ARG_PRICE_PER_DAY = "price_per_day";
    private static final String ARG_GUESTS = "guests";
    private static final String ARG_CHECKIN = "checkin";
    private static final String ARG_CHECKOUT = "checkout";

    // UI Components
    private TextView tvPricePerDay, tvDates, tvGuests, tvTotalDays, tvTax, tvTotalPrice;
    private TextView tvPlaceName, tvPlaceAddress, tvCheckinDate, tvCheckoutDate;
    private ImageView imgPlace;
    private Button btnProceedPay;
    private Toolbar toolbar;

    // Data
    private String placeId, placeName, placeAddress, placeImageUrl;
    private int pricePerDay, guests;
    private long checkinTime, checkoutTime;

    private ApiService apiService;

    public static ConfirmBookingFragment newInstance(String placeId, String placeName, String placeAddress,
            String placeImageUrl, int pricePerDay, int guests,
            long checkinTime, long checkoutTime) {
        ConfirmBookingFragment fragment = new ConfirmBookingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE_ID, placeId);
        args.putString(ARG_PLACE_NAME, placeName);
        args.putString(ARG_PLACE_ADDRESS, placeAddress);
        args.putString(ARG_PLACE_IMAGE, placeImageUrl);
        args.putInt(ARG_PRICE_PER_DAY, pricePerDay);
        args.putInt(ARG_GUESTS, guests);
        args.putLong(ARG_CHECKIN, checkinTime);
        args.putLong(ARG_CHECKOUT, checkoutTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString(ARG_PLACE_ID);
            placeName = getArguments().getString(ARG_PLACE_NAME);
            placeAddress = getArguments().getString(ARG_PLACE_ADDRESS);
            placeImageUrl = getArguments().getString(ARG_PLACE_IMAGE);
            pricePerDay = getArguments().getInt(ARG_PRICE_PER_DAY);
            guests = getArguments().getInt(ARG_GUESTS);
            checkinTime = getArguments().getLong(ARG_CHECKIN);
            checkoutTime = getArguments().getLong(ARG_CHECKOUT);
        }
        apiService = ApiClient.getClient(getContext()).create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide Bottom Nav
        if (getActivity() instanceof BaseActivity) {
            // This is handled by BaseActivity logic based on backstack but just in case
        }

        initViews(view);
        setupData();
        setupListeners();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvPricePerDay = view.findViewById(R.id.tv_price_per_day);
        tvDates = view.findViewById(R.id.tv_dates);
        tvGuests = view.findViewById(R.id.tv_guests);
        tvTotalDays = view.findViewById(R.id.tv_total_days);
        tvTax = view.findViewById(R.id.tv_tax);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);

        tvPlaceName = view.findViewById(R.id.tv_place_name);
        tvPlaceAddress = view.findViewById(R.id.tv_place_address);
        tvCheckinDate = view.findViewById(R.id.tv_checkin_date);
        tvCheckoutDate = view.findViewById(R.id.tv_checkout_date);

        imgPlace = view.findViewById(R.id.img_place);
        btnProceedPay = view.findViewById(R.id.btn_proceed_payment);
    }

    private void setupData() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        SimpleDateFormat sdfFull = new SimpleDateFormat("MMM dd EEE", Locale.getDefault());

        String dateRange = sdf.format(new Date(checkinTime)) + "-" + sdf.format(new Date(checkoutTime));
        long diff = checkoutTime - checkinTime;
        long days = diff / (24 * 60 * 60 * 1000);
        if (days < 1)
            days = 1;

        double subtotal = pricePerDay * days * guests; // Assuming price is per person per day or just per day?
        // Usually price is per room/place per night. Let's assume price is per NIGHT.
        // And guests doesn't multiply price unless logic says so.
        // User's logic in booking: totalPrice = diffHours * placeStatus.price *
        // numberOfPeople;
        // So YES, it multiplies by people.

        double total = (pricePerDay * days * guests);
        double tax = 1.50; // Mock tax as per screenshot
        double grandTotal = total + tax;

        tvPricePerDay.setText("$" + pricePerDay);
        tvDates.setText(dateRange);
        tvGuests.setText(guests + " Guest");
        tvTotalDays.setText(days + " Days");
        tvTax.setText("$" + String.format("%.2f", tax));
        tvTotalPrice.setText("$" + String.format("%.2f", grandTotal));

        tvPlaceName.setText(placeName);
        tvPlaceAddress.setText(placeAddress);

        tvCheckinDate.setText(sdfFull.format(new Date(checkinTime)));
        tvCheckoutDate.setText(sdfFull.format(new Date(checkoutTime)));

        if (placeImageUrl != null) {
            Glide.with(this).load(placeImageUrl).into(imgPlace);
        }
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnProceedPay.setOnClickListener(v -> submitBooking());
    }

    private void submitBooking() {
        // Navigate to Success Screen directly (API call is moved to
        // ReviewReservationFragment)
        BookingSuccessFragment successFragment = BookingSuccessFragment.newInstance(
                placeId,
                placeName,
                tvDates.getText().toString() + " (" + tvTotalDays.getText().toString() + ")",
                guests,
                tvTotalPrice.getText().toString(),
                checkinTime,
                checkoutTime);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.base_container, successFragment)
                .addToBackStack(null)
                .commit();
    }
}
