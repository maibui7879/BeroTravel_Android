package com.example.berotravel20.ui.main.booking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.berotravel20.R;
import com.example.berotravel20.ui.common.BaseFragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingTabFragment extends BaseFragment {
    private String placeId, placeName, placeAddress, placeImage;
    private int price;
    private TextView tvCheckin, tvCheckout;
    private EditText etPeople;
    private Calendar checkinDate = Calendar.getInstance();
    private Calendar checkoutDate = Calendar.getInstance();
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public static BookingTabFragment newInstance(String id, String name, String addr, String img, int price) {
        BookingTabFragment f = new BookingTabFragment();
        Bundle b = new Bundle();
        b.putString("KEY_PLACE_ID", id);
        b.putString("KEY_PLACE_NAME", name);
        b.putString("KEY_PLACE_ADDR", addr);
        b.putString("KEY_PLACE_IMG", img);
        b.putInt("KEY_PRICE", price);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("KEY_PLACE_ID");
            placeName = getArguments().getString("KEY_PLACE_NAME");
            placeAddress = getArguments().getString("KEY_PLACE_ADDR");
            placeImage = getArguments().getString("KEY_PLACE_IMG");
            price = getArguments().getInt("KEY_PRICE");
        }
        checkoutDate.add(Calendar.DAY_OF_MONTH, 1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_booking_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCheckin = view.findViewById(R.id.btn_checkin);
        tvCheckout = view.findViewById(R.id.btn_checkout);
        etPeople = view.findViewById(R.id.et_people_count);

        updateDateLabels();
        tvCheckin.setOnClickListener(v -> showDateTimePicker(true));
        tvCheckout.setOnClickListener(v -> showDateTimePicker(false));
        view.findViewById(R.id.btn_book_now).setOnClickListener(v -> navigateToConfirm());
    }

    private void updateDateLabels() {
        tvCheckin.setText(displayFormat.format(checkinDate.getTime()));
        tvCheckout.setText(displayFormat.format(checkoutDate.getTime()));
    }

    private void showDateTimePicker(boolean isCheckin) {
        Calendar target = isCheckin ? checkinDate : checkoutDate;
        new DatePickerDialog(requireContext(), (v, year, month, dayOfMonth) -> {
            target.set(Calendar.YEAR, year);
            target.set(Calendar.MONTH, month);
            target.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(requireContext(), (v1, hour, min) -> {
                target.set(Calendar.HOUR_OF_DAY, hour);
                target.set(Calendar.MINUTE, min);
                if (!isCheckin && !target.after(checkinDate)) {
                    showError("Ngày trả phải sau ngày nhận!");
                }
                updateDateLabels();
            }, target.get(Calendar.HOUR_OF_DAY), target.get(Calendar.MINUTE), true).show();
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void navigateToConfirm() {
        String peopleStr = etPeople.getText().toString().trim();
        if (peopleStr.isEmpty()) { showError("Vui lòng nhập số khách"); return; }

        ConfirmBookingFragment confirm = ConfirmBookingFragment.newInstance(
                placeId, placeName, placeAddress, placeImage, price,
                Integer.parseInt(peopleStr), checkinDate.getTimeInMillis(), checkoutDate.getTimeInMillis());
        replaceFragment(confirm);
    }
}