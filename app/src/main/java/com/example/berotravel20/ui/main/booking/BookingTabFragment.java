package com.example.berotravel20.ui.main.booking;

import android.app.DatePickerDialog;
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
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static BookingTabFragment newInstance(String id, String name, String addr, String img, int price) {
        BookingTabFragment f = new BookingTabFragment();
        Bundle b = new Bundle();
        b.putString("id", id); b.putString("name", name); b.putString("addr", addr);
        b.putString("img", img); b.putInt("price", price);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("id");
            placeName = getArguments().getString("name");
            placeAddress = getArguments().getString("addr");
            placeImage = getArguments().getString("img");
            price = getArguments().getInt("price");
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
        tvCheckin.setOnClickListener(v -> showDatePicker(true));
        tvCheckout.setOnClickListener(v -> showDatePicker(false));
        view.findViewById(R.id.btn_book_now).setOnClickListener(v -> navigateToConfirm());
    }

    private void updateDateLabels() {
        tvCheckin.setText(displayFormat.format(checkinDate.getTime()));
        tvCheckout.setText(displayFormat.format(checkoutDate.getTime()));
    }

    private void showDatePicker(boolean isCheckin) {
        Calendar target = isCheckin ? checkinDate : checkoutDate;
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (v, y, m, d) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(y, m, d);
            if (isCheckin) {
                checkinDate = selected;
                if (!checkinDate.before(checkoutDate)) {
                    checkoutDate = (Calendar) checkinDate.clone();
                    checkoutDate.add(Calendar.DAY_OF_MONTH, 1);
                }
            } else {
                if (!selected.after(checkinDate)) {
                    showError("Ngày trả phòng phải sau ngày nhận!");
                    return;
                }
                checkoutDate = selected;
            }
            updateDateLabels();
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void navigateToConfirm() {
        String peopleStr = etPeople.getText().toString().trim();
        if (peopleStr.isEmpty() || peopleStr.equals("0")) {
            showError("Vui lòng nhập số khách"); return;
        }
        ConfirmBookingFragment confirm = ConfirmBookingFragment.newInstance(
                placeId, placeName, placeAddress, placeImage, price,
                Integer.parseInt(peopleStr), checkinDate.getTimeInMillis(), checkoutDate.getTimeInMillis());
        replaceFragment(confirm);
    }
}