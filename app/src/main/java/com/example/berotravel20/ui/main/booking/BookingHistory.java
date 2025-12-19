package com.example.berotravel20.ui.main.booking;

import android.app.Dialog;
import android.content.Intent;
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
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.Booking.Booking;
import com.example.berotravel20.data.repository.BookingRepository;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.common.RequestLoginDialog;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class BookingHistory extends BaseFragment implements BookingHistoryAdapter.OnBookingActionListener, RequestLoginDialog.RequestLoginListener {

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
        // Lắng nghe tín hiệu làm mới sau khi thanh toán hoặc đặt chỗ thành công từ Fragment khác
        getParentFragmentManager().setFragmentResultListener("booking_request_key", this, (requestKey, bundle) -> {
            if (bundle.getBoolean("refresh_data")) {
                fetchData(false);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. KIỂM TRA ĐĂNG NHẬP
        if (!isUserLoggedIn()) {
            showLoginRequestDialog();
            return;
        }

        // 2. KHỞI TẠO UI VÀ REPOSITORY
        initViews(view);
        bookingRepository = new BookingRepository();

        // 3. TẢI DỮ LIỆU LẦN ĐẦU
        fetchData(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tự động làm mới khi quay lại tab nếu đã đăng nhập
        if (isUserLoggedIn()) {
            fetchData(false);
        }
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
        // Cấu hình lọc trạng thái
        String[] filters = {"Tất cả", "Đã thanh toán", "Chờ thanh toán"};
        autoFilter.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, filters));
        autoFilter.setOnItemClickListener((p, v, pos, id) -> processFilterAndSort());

        // Cấu hình sắp xếp thời gian
        String[] sorts = {"Mới nhất", "Cũ nhất"};
        autoSort.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sorts));
        autoSort.setOnItemClickListener((p, v, pos, id) -> processFilterAndSort());
    }

    private void fetchData(boolean showLoader) {
        if (!isUserLoggedIn()) return;

        if (showLoader) showLoading();

        // Lấy ID người dùng từ TokenManager
        String userId = TokenManager.getInstance(requireContext()).getUserId();

        bookingRepository.getBookings(userId, new DataCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> data) {
                if (showLoader) hideLoading();
                swipeRefresh.setRefreshing(false);
                originalList = (data != null) ? data : new ArrayList<>();
                processFilterAndSort();
            }

            @Override
            public void onError(String msg) {
                if (showLoader) hideLoading();
                swipeRefresh.setRefreshing(false);
                showError(msg);
            }
        });
    }

    // Xử lý logic Filter và Sort cục bộ trên danh sách đã tải về
    private void processFilterAndSort() {
        if (originalList == null) return;
        List<Booking> filtered = new ArrayList<>(originalList);

        // 1. Lọc theo trạng thái thanh toán
        String fText = autoFilter.getText().toString();
        if (fText.equals("Đã thanh toán")) {
            filtered.removeIf(b -> !b.isPaid);
        } else if (fText.equals("Chờ thanh toán")) {
            filtered.removeIf(b -> b.isPaid);
        }

        // 2. Sắp xếp theo ngày (Chuỗi ISO 8601)
        String sText = autoSort.getText().toString();
        Collections.sort(filtered, (b1, b2) -> {
            if (b1.bookingDateTime == null || b2.bookingDateTime == null) return 0;
            return sText.equals("Mới nhất")
                    ? b2.bookingDateTime.compareTo(b1.bookingDateTime)
                    : b1.bookingDateTime.compareTo(b2.bookingDateTime);
        });

        // 3. Cập nhật Adapter
        if (adapter == null) {
            adapter = new BookingHistoryAdapter(filtered, this);
            rvHistory.setAdapter(adapter);
        } else {
            adapter.updateList(filtered);
        }

        // 4. Hiển thị Empty State nếu không có đơn nào
        llEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        // 5. Chạy hiệu ứng xuất hiện danh sách
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        rvHistory.setLayoutAnimation(animation);
        rvHistory.scheduleLayoutAnimation();
    }

    @Override
    public void onPay(Booking booking) {
        if (booking == null || booking.place == null) return;

        long start = parseIsoDate(booking.bookingDateTime);
        long end = parseIsoDate(booking.checkoutDateTime);

        // CHỮA CHÁY: Chia giá cho 24 trước khi truyền vào Fragment thanh toán
        int fixedPrice = (int) (booking.place.price / 24);

        ReviewReservationFragment review = ReviewReservationFragment.newInstance(
                booking.place.id,
                booking.place.name,
                booking.place.address,
                booking.place.imageUrl,
                fixedPrice, // Sử dụng giá đã sửa
                booking.numberOfPeople,
                start,
                end
        );
        replaceFragment(review);
    }

    @Override
    public void onCancel(Booking booking) {
        // Hiển thị Dialog xác nhận hủy
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_confirm);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvMsg = dialog.findViewById(R.id.dialog_message);
        tvMsg.setText("Hủy đơn đặt chỗ tại " + booking.place.name + "?");

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
            public void onSuccess(Void d) {
                hideLoading();
                showSuccess("Đã hủy đơn thành công");
                fetchData(false);
            }

            @Override
            public void onError(String m) {
                hideLoading();
                showError(m);
            }
        });
    }

    @Override
    public void onItemClick(Booking booking) {
        // Xử lý khi click vào item (Xem chi tiết đơn hàng)
        showSuccess("Mã đơn: " + booking.id);
    }

    private long parseIsoDate(String isoDate) {
        if (isoDate == null) return System.currentTimeMillis();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(isoDate).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private void showLoginRequestDialog() {
        RequestLoginDialog dialog = RequestLoginDialog.newInstance();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "RequestLoginDialog");
    }

    @Override
    public void onLoginClick() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCancelClick() {
        // Có thể quay về màn hình chính hoặc đóng fragment
    }
}