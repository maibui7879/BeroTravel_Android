package com.example.berotravel20.ui.common;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.SearchSelectAdapter; // Dùng Adapter mới
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceResponse;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.ui.map.LocationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;

public class SearchPlaceBottomSheet extends BottomSheetDialogFragment {

    public interface OnPlaceSelectedListener {
        void onPlaceSelected(Place place);
    }

    private OnPlaceSelectedListener listener;
    private PlaceRepository placeRepository;
    private LocationHelper locationHelper;
    private SearchSelectAdapter adapter; // Adapter mới

    private EditText etSearch;
    private Slider sliderRadius;
    private TextView tvRadiusLabel;
    private RecyclerView rvResults;
    private Button btnSearch;
    private int currentRadius = 5;

    public void setListener(OnPlaceSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_search_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeRepository = new PlaceRepository();
        locationHelper = new LocationHelper(requireActivity());

        etSearch = view.findViewById(R.id.et_search_keyword);
        sliderRadius = view.findViewById(R.id.slider_radius);
        tvRadiusLabel = view.findViewById(R.id.tv_radius_label);
        rvResults = view.findViewById(R.id.rv_search_results);
        btnSearch = view.findViewById(R.id.btn_search_action);

        // Khởi tạo Adapter mới với logic Click
        adapter = new SearchSelectAdapter(place -> {
            if (listener != null) {
                listener.onPlaceSelected(place); // Trả kết quả về Fragment cha
                dismiss(); // Đóng ngay BottomSheet
            }
        });

        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvResults.setAdapter(adapter);

        sliderRadius.addOnChangeListener((slider, value, fromUser) -> {
            currentRadius = (int) value;
            tvRadiusLabel.setText("Bán kính tìm kiếm: " + currentRadius + " km");
        });

        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập từ khóa", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSearch.setText("Đang tìm...");
        btnSearch.setEnabled(false);

        locationHelper.getLastLocation(location -> {
            if (location != null) {
                callSearchApi(location.getLatitude(), location.getLongitude(), keyword);
            } else {
                callSearchApi(21.0285, 105.8542, keyword); // Fallback Hà Nội
            }
        });
    }

    private void callSearchApi(double lat, double lng, String keyword) {
        placeRepository.searchNearby(lat, lng, currentRadius, keyword, null, 1, 20, new DataCallback<PlaceResponse>() {
            @Override
            public void onSuccess(PlaceResponse data) {
                if (!isAdded()) return; // Chặn crash nếu user đóng sheet quá nhanh
                btnSearch.setText("Tìm kiếm");
                btnSearch.setEnabled(true);

                if (data != null && data.data != null && !data.data.isEmpty()) {
                    adapter.setData(data.data); // Cập nhật dữ liệu cho Adapter mới
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String msg) {
                if (!isAdded()) return;
                btnSearch.setText("Tìm kiếm");
                btnSearch.setEnabled(true);
                Toast.makeText(getContext(), "Lỗi: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}