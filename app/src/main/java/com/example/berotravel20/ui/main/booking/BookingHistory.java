package com.example.berotravel20.ui.main.booking;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.BookingHistoryAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Booking.Booking;
import com.example.berotravel20.data.repository.BookingRepository;
import com.example.berotravel20.ui.common.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookingHistory extends BaseFragment implements BookingHistoryAdapter.OnBookingActionListener {
    private RecyclerView rvHistory;
    private LinearLayout llEmpty;
    private AutoCompleteTextView autoFilter, autoSort;
    private SwipeRefreshLayout swipeRefresh;
    private BookingRepository bookingRepository;
    private List<Booking> originalList = new ArrayList<>();
    private BookingHistoryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lắng nghe tín hiệu làm mới sau khi thanh toán thành công
        getParentFragmentManager().setFragmentResultListener("booking_request_key", this, (requestKey, bundle) -> {
            if (bundle.getBoolean("refresh_data")) { fetchData(false); }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        bookingRepository = new BookingRepository();
        if (isUserLoggedIn()) fetchData(true);
    }

    private void initViews(View view) {
        rvHistory = view.findViewById(R.id.rv_booking_history);
        llEmpty = view.findViewById(R.id.ll_empty_state);
        autoFilter = view.findViewById(R.id.auto_filter);
        autoSort = view.findViewById(R.id.auto_sort);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        swipeRefresh.setColorSchemeResources(R.color.teal_700);
        swipeRefresh.setOnRefreshListener(() -> fetchData(false));

        setupDropdownMenus();
    }

    private void setupDropdownMenus() {
        // Cấu hình Exposed Dropdown chuyên nghiệp
        String[] filters = {"Tất cả", "Đã thanh toán", "Chờ thanh toán"};
        autoFilter.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, filters));
        autoFilter.setOnItemClickListener((p, v, pos, id) -> processFilterAndSort());

        String[] sorts = {"Mới nhất", "Cũ nhất"};
        autoSort.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sorts));
        autoSort.setOnItemClickListener((p, v, pos, id) -> processFilterAndSort());
    }

    private void fetchData(boolean showLoader) {
        if (showLoader) showLoading();
        bookingRepository.getBookings(tokenManager.getUserId(), new DataCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> data) {
                if (showLoader) hideLoading();
                swipeRefresh.setRefreshing(false);
                originalList = data;
                processFilterAndSort();
            }
            @Override public void onError(String msg) {
                swipeRefresh.setRefreshing(false); hideLoading(); showError(msg);
            }
        });
    }

    @Override
    public void onPay(Booking booking) {
        if (booking == null || booking.place == null) return;

        // CHỈNH SỬA: Chuyển dữ liệu và điều hướng sang trang xác nhận
        long start = parseIsoDate(booking.bookingDateTime);
        long end = parseIsoDate(booking.checkoutDateTime);

        ReviewReservationFragment review = ReviewReservationFragment.newInstance(
                booking.place.id, booking.place.name, booking.place.address,
                booking.place.imageUrl, (int) booking.place.price,
                booking.numberOfPeople, start, end
        );
        replaceFragment(review);
    }

    @Override
    public void onCancel(Booking booking) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_confirm);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ((TextView) dialog.findViewById(R.id.dialog_message)).setText("Hủy đơn tại " + booking.place.name + "?");
        dialog.findViewById(R.id.btn_negative).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_positive).setOnClickListener(v -> {
            dialog.dismiss();
            deleteBooking(booking.id);
        });
        dialog.show();
    }

    private void deleteBooking(String id) {
        showLoading();
        bookingRepository.deleteBooking(id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void d) { hideLoading(); showSuccess("Đã hủy đơn"); fetchData(false); }
            @Override public void onError(String m) { hideLoading(); showError(m); }
        });
    }

    private void processFilterAndSort() {
        if (originalList == null) return;
        List<Booking> filtered = new ArrayList<>(originalList);

        String fText = autoFilter.getText().toString();
        if (fText.equals("Đã thanh toán")) filtered.removeIf(b -> !b.isPaid);
        else if (fText.equals("Chờ thanh toán")) filtered.removeIf(b -> b.isPaid);

        String sText = autoSort.getText().toString();
        Collections.sort(filtered, (b1, b2) -> sText.equals("Mới nhất")
                ? b2.bookingDateTime.compareTo(b1.bookingDateTime)
                : b1.bookingDateTime.compareTo(b2.bookingDateTime));

        if (adapter == null) {
            adapter = new BookingHistoryAdapter(filtered, this);
            rvHistory.setAdapter(adapter);
        } else {
            adapter.updateList(filtered);
        }

        llEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        // SỬA LỖI ANIMATION: Trỏ đúng vào file LayoutAnimation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        rvHistory.setLayoutAnimation(animation);
        rvHistory.scheduleLayoutAnimation();
    }

    @Override public void onItemClick(Booking booking) { showSuccess("Mã đơn: " + booking.id); }

    private long parseIsoDate(String isoDate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.parse(isoDate).getTime();
        } catch (Exception e) { return System.currentTimeMillis(); }
    }
}