package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.berotravel20.R;
import com.example.berotravel20.ui.common.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewReservationFragment extends Fragment {

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

    private com.example.berotravel20.network.ApiService apiService;

    public static ReviewReservationFragment newInstance(String placeId, String placeName, String dates, int guests,
            String total, long checkinMillis, long checkoutMillis) {
        ReviewReservationFragment fragment = new ReviewReservationFragment();
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
        apiService = com.example.berotravel20.network.ApiClient.getClient(getContext())
                .create(com.example.berotravel20.network.ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView tvPlaceName = view.findViewById(R.id.tv_place_name_review);
        tvPlaceName.setText(placeName);

        TextView tvDates = view.findViewById(R.id.tv_stay_dates);
        tvDates.setText(datesStr); // "Feb 06-09 (3 Nights)" - assume passed correctly or format it here

        TextView tvGuests = view.findViewById(R.id.tv_stay_guests);
        tvGuests.setText("1 Room, " + guests + " Guests");

        TextView tvTotal = view.findViewById(R.id.tv_total_review);
        tvTotal.setText(totalStr);

        Button btnBookNow = view.findViewById(R.id.btn_book_now);
        // "Book Now" in user flow usually means "Done/Return Home" effectively since
        // booking is mostly done or this is a final confirmation
        // User said: "Confirm(Fake skip) -> Review". And this Review screen has "Book
        // Now" at bottom.
        // It's a bit duplicate naming but let's assume it finishes the flow.

        btnBookNow.setOnClickListener(v -> submitBooking());

        // Populate charges breakdown dynamically if needed,
        // For now the layout has placeholders. We can leave it or try to populate based
        // on checkinMillis.
        // Let's rely on placeholders for simplicity unless user asks for exact dates.
    }

    private void submitBooking() {
        android.util.Log.d("ReviewReservation", "submitBooking called");

        // Debug inputs
        android.util.Log.d("ReviewReservation", "Inputs: placeId=" + placeId + ", guests=" + guests + ", checkin="
                + checkinMillis + ", checkout=" + checkoutMillis);

        if (placeId == null) {
            android.widget.Toast
                    .makeText(getContext(), "Error: Place ID is missing!", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String checkinIsoStr = isoFormat.format(new Date(checkinMillis)) + "T12:00:00Z";
        String checkoutIsoStr = isoFormat.format(new Date(checkoutMillis)) + "T12:00:00Z";

        android.util.Log.d("ReviewReservation", "Formatted Dates: " + checkinIsoStr + " to " + checkoutIsoStr);

        com.example.berotravel20.models.BookingRequest request = new com.example.berotravel20.models.BookingRequest(
                placeId, guests, checkinIsoStr, checkoutIsoStr);

        apiService.createBooking(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                android.util.Log.d("ReviewReservation", "API Response Code: " + response.code());
                if (response.isSuccessful()) {
                    android.util.Log.d("ReviewReservation", "API Success");
                    android.widget.Toast
                            .makeText(getContext(), "Booking Saved Successfully!", android.widget.Toast.LENGTH_LONG)
                            .show();
                    // Clear back stack to Home
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    android.util.Log.e("ReviewReservation", "API Error: " + response.message());
                    try {
                        android.util.Log.e("ReviewReservation", "Error Body: " + response.errorBody().string());
                    } catch (Exception e) {
                    }

                    android.widget.Toast.makeText(getContext(), "Booking Failed: " + response.code(),
                            android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                android.util.Log.e("ReviewReservation", "API Failure: " + t.getMessage(), t);
                android.widget.Toast
                        .makeText(getContext(), "Error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
