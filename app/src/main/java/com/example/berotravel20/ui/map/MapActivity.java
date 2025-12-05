package com.example.berotravel20.ui.map;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private RecyclerView rvResults;
    private MapPlaceAdapter adapter;
    private EditText etSearch;
    private TextView tvResultCount;

    // Repository lấy dữ liệu
    private PlaceRepository placeRepository;

    // Map để lưu trữ Marker và Place tương ứng (để khi click marker biết là place nào)
    private Map<String, Place> markerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 1. Khởi tạo Repository
        placeRepository = new PlaceRepository();

        // 2. Setup View
        initViews();

        // 3. Setup Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initViews() {
        // Search Bar
        etSearch = findViewById(R.id.etSearchMap);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString();
                performSearch(query);
                return true;
            }
            return false;
        });

        // Bottom Sheet
        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        tvResultCount = findViewById(R.id.tvResultCount); // Bạn cần thêm ID này vào layout xml phần text "Kết quả tìm kiếm (3)"

        // RecyclerView
        rvResults = findViewById(R.id.rvMapResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // Adapter: Khi click item trong list -> Di chuyển camera tới đó
        adapter = new MapPlaceAdapter(this, place -> {
            if (mMap != null) {
                LatLng location = new LatLng(place.latitude, place.longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Thu nhỏ để nhìn thấy map
            }
        });
        rvResults.setAdapter(adapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Cấu hình UI Map
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Di chuyển camera mặc định về Hà Nội (hoặc vị trí hiện tại nếu có quyền)
        LatLng hanoi = new LatLng(21.0285, 105.8542);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 14f));

        // Load dữ liệu ban đầu (Tất cả địa điểm)
        loadAllPlaces();

        // Sự kiện click Marker
        mMap.setOnMarkerClickListener(marker -> {
            Place place = markerMap.get(marker.getId());
            if (place != null) {
                // Hiển thị tên trong InfoWindow (hoặc custom layout)
                marker.showInfoWindow();
                // Có thể scroll list tới vị trí của place này (Nâng cao)
            }
            return false;
        });
    }

    // --- LOGIC GỌI API ---

    private void loadAllPlaces() {
        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                updateMapAndList(data);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(MapActivity.this, "Lỗi tải map: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch(String keyword) {
        if (mMap == null) return;

        // Lấy vị trí tâm màn hình hiện tại để tìm kiếm xung quanh
        LatLng center = mMap.getCameraPosition().target;

        // Gọi API Search Nearby (Bán kính 10km)
        placeRepository.searchNearby(center.latitude, center.longitude, 10, keyword, null, new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                updateMapAndList(data);
                if (data.isEmpty()) {
                    Toast.makeText(MapActivity.this, "Không tìm thấy kết quả nào", Toast.LENGTH_SHORT).show();
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Mở list ra xem
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(MapActivity.this, "Lỗi tìm kiếm: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm cập nhật chung cho Map và List
    private void updateMapAndList(List<Place> places) {
        if (places == null) return;

        // 1. Cập nhật List (RecyclerView)
        adapter.setData(places);
        if (tvResultCount != null) {
            tvResultCount.setText("Kết quả tìm kiếm (" + places.size() + ")");
        }

        // 2. Cập nhật Map (Markers)
        mMap.clear(); // Xóa marker cũ
        markerMap.clear();

        for (Place place : places) {
            LatLng location = new LatLng(place.latitude, place.longitude);

            // Tạo Marker
            MarkerOptions options = new MarkerOptions()
                    .position(location)
                    .title(place.name)
                    .snippet(place.address); // Có thể thêm giá vào đây nếu muốn

            Marker marker = mMap.addMarker(options);

            // Lưu reference để xử lý click
            if (marker != null) {
                markerMap.put(marker.getId(), place);
            }
        }
    }
}