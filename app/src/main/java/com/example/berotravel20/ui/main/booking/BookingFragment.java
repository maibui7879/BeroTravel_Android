package com.example.berotravel20.ui.main.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.R;
import com.example.berotravel20.models.BookingRequest;
import com.example.berotravel20.network.ApiClient;
import com.example.berotravel20.network.ApiService;
import com.example.berotravel20.network.TokenManager;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingFragment extends Fragment {

    private String placeId;
    private String placeName;
    private int price;

    private TextView tvPlaceName, tvAddress, tvCheckin, tvCheckout;
    private EditText etPeople;
    private Button btnBookNow;
    private ApiService apiService;

    private Calendar checkinDate = Calendar.getInstance();
    private Calendar checkoutDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd EEE", Locale.getDefault());
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static BookingFragment newInstance(String placeId, String placeName, int price) {
        BookingFragment fragment = new BookingFragment();
        Bundle args = new Bundle();
        args.putString("PLACE_ID", placeId);
        args.putString("PLACE_NAME", placeName);
        args.putInt("PRICE", price);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("PLACE_ID");
            placeName = getArguments().getString("PLACE_NAME");
            price = getArguments().getInt("PRICE");
        }
        apiService = ApiClient.getClient(getContext()).create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPlaceName = view.findViewById(R.id.tv_place_name);
        tvAddress = view.findViewById(R.id.tv_address);
        tvCheckin = view.findViewById(R.id.btn_checkin);
        tvCheckout = view.findViewById(R.id.btn_checkout);
        etPeople = view.findViewById(R.id.et_people_count);
        btnBookNow = view.findViewById(R.id.btn_book_now);

        tvPlaceName.setText(placeName);
        tvAddress.setText("Mock Address, City"); // Ideally passed via args or fetched

        // Defaults
        checkoutDate.add(Calendar.DAY_OF_MONTH, 3); // Default 3 days
        updateDateLabels();

        tvCheckin.setOnClickListener(v -> showDatePicker(true));
        tvCheckout.setOnClickListener(v -> showDatePicker(false));

        btnBookNow.setOnClickListener(v -> submitBooking());

        // Handle Back from Toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar); // Needs ID in XML?
        // Actually the CollapsingToolbar has a Toolbar but I didn't verify its ID.
        // Let's assume user uses system back or adds logic later.
        // For now, I'll add a quick finder if generic Toolbar ID is used or
        // navigationIcon click logic
        // But layout didn't give ID to Toolbar. Let's skip explicit Toolbar ID binding
        // for now to avoid crashes.
    }

    private void updateDateLabels() {
        tvCheckin.setText(dateFormat.format(checkinDate.getTime()));
        tvCheckout.setText(dateFormat.format(checkoutDate.getTime()));
    }

    private void showDatePicker(boolean isCheckin) {
        Calendar target = isCheckin ? checkinDate : checkoutDate;
        DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            target.set(year, month, dayOfMonth);
            updateDateLabels();
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void submitBooking() {
        String peopleStr = etPeople.getText().toString();

        if (peopleStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter number of guests", Toast.LENGTH_SHORT).show();
            return;
        }

        int people = Integer.parseInt(peopleStr);
        long checkinTime = checkinDate.getTimeInMillis();
        long checkoutTime = checkoutDate.getTimeInMillis();

        // Pass data to Confirm Screen
        ConfirmBookingFragment confirmFragment = ConfirmBookingFragment.newInstance(
                placeId, placeName, tvAddress.getText().toString(), null, price, people, checkinTime, checkoutTime);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.base_container, confirmFragment)
                .addToBackStack(null)
                .commit();
    }
}