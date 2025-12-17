package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.ui.common.BaseFragment;
import java.util.ArrayList;

public class BookingFragment extends BaseFragment {
    private String placeId, placeName, placeAddress, placeImage, category;
    private int price, startTab;
    private ArrayList<String> imgSet;
    private TextView tabReviews, tabPhotos, tabBooking;

    // Cập nhật newInstance nhận thêm biến category
    public static BookingFragment newInstance(String id, String name, String addr, String img, int price, String category, ArrayList<String> images, int startTab) {
        BookingFragment f = new BookingFragment();
        Bundle a = new Bundle();
        a.putString("PLACE_ID", id);
        a.putString("PLACE_NAME", name);
        a.putString("PLACE_ADDRESS", addr);
        a.putString("PLACE_IMAGE", img);
        a.putInt("PRICE", price);
        a.putString("CATEGORY", category); // Lưu category
        a.putStringArrayList("IMG_SET", images);
        a.putInt("START_TAB", startTab);
        f.setArguments(a);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("PLACE_ID");
            placeName = getArguments().getString("PLACE_NAME");
            placeAddress = getArguments().getString("PLACE_ADDRESS");
            placeImage = getArguments().getString("PLACE_IMAGE");
            price = getArguments().getInt("PRICE");
            category = getArguments().getString("CATEGORY");
            imgSet = getArguments().getStringArrayList("IMG_SET");

            // Mặc định startTab là 0 (Đánh giá) theo yêu cầu mới
            startTab = getArguments().getInt("START_TAB", 0);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        // Kiểm tra nếu Tab hiện tại là Booking nhưng category không hợp lệ thì chuyển về Review
        if (startTab == 2 && !isAccommodation()) {
            startTab = 0;
        }

        selectTab(startTab);
    }

    private void initViews(View view) {
        ((TextView)view.findViewById(R.id.tv_place_name)).setText(placeName);
        ((TextView)view.findViewById(R.id.tv_address)).setText(placeAddress);
        Glide.with(this).load(placeImage).into((ImageView) view.findViewById(R.id.img_header));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBack());

        tabReviews = view.findViewById(R.id.tab_reviews);
        tabPhotos = view.findViewById(R.id.tab_photos);
        tabBooking = view.findViewById(R.id.tab_booking);

        // --- LOGIC KIỂM TRA CATEGORY ---
        if (isAccommodation()) {
            tabBooking.setVisibility(View.VISIBLE);
        } else {
            // Nếu không phải nhóm lưu trú thì ẩn hẳn nút Đặt chỗ
            tabBooking.setVisibility(View.GONE);
        }

        tabReviews.setOnClickListener(v -> selectTab(0));
        tabPhotos.setOnClickListener(v -> selectTab(1));
        tabBooking.setOnClickListener(v -> selectTab(2));
    }

    /**
     * Kiểm tra xem địa điểm có thuộc nhóm cho phép đặt phòng hay không
     */
    private boolean isAccommodation() {
        if (category == null) return false;
        String cat = category.toLowerCase();
        // Kiểm tra các từ khóa liên quan đến lưu trú
        return cat.contains("khách sạn") ||
                cat.contains("hotel") ||
                cat.contains("resort") ||
                cat.contains("homestay") ||
                cat.contains("nhà nghỉ");
    }

    private void selectTab(int index) {
        updateTabUI(index);
        BaseFragment fragment;
        switch (index) {
            case 0:
                fragment = ReviewTabFragment.newInstance(placeId);
                break;
            case 1:
                fragment = PhotoTabFragment.newInstance(placeId, placeImage, imgSet);
                break;
            case 2:
                // Chỉ cho phép mở BookingTab nếu là nhóm lưu trú
                if (isAccommodation()) {
                    fragment = BookingTabFragment.newInstance(placeId, placeName, placeAddress, placeImage, price);
                } else {
                    // Fallback về Review nếu cố tình truy cập trái phép
                    updateTabUI(0);
                    fragment = ReviewTabFragment.newInstance(placeId);
                }
                break;
            default:
                fragment = ReviewTabFragment.newInstance(placeId);
                break;
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.tab_container, fragment)
                .commit();
    }

    private void updateTabUI(int index) {
        if (!isAdded()) return;

        int active = ContextCompat.getColor(requireContext(), R.color.teal_700);
        int gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray);

        tabReviews.setTextColor(index == 0 ? active : gray);
        tabPhotos.setTextColor(index == 1 ? active : gray);
        tabBooking.setTextColor(index == 2 ? active : gray);

        tabReviews.setTypeface(null, index == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabPhotos.setTypeface(null, index == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabBooking.setTypeface(null, index == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }
}