package com.example.berotravel20.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.utils.CategoryUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MAP_DEBUG";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Location currentUserLocation;

    // UI Components
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private RecyclerView rvResults;
    private MapPlaceAdapter adapter;
    private EditText etSearch;
    private TextView tvResultCount;
    private ProgressBar pbLoading;
    private ChipGroup chipGroupFilter;

    private PlaceRepository placeRepository;
    private Map<String, Place> markerMap = new HashMap<>();

    // Biến lưu trạng thái
    private String currentKeyword = "";
    private String currentCategory = null;

    private final String[] POPULAR_FILTERS = {
            "restaurant", "cafe", "hotel", "atm", "gas_station", "hospital"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate: MapActivity started");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        placeRepository = new PlaceRepository();

        initViews();
        setupFilterChips();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearchMap);

        // Sự kiện: Bấm Enter trên bàn phím -> GỌI API SEARCH
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                handleSearchAction(); // Chỉ gọi search khi bấm Enter
                return true;
            }
            return false;
        });

        // Sự kiện: Bấm nút Kính lúp -> GỌI API SEARCH
        ImageView btnSearchIcon = findViewById(R.id.btnSearchIcon);
        btnSearchIcon.setOnClickListener(v -> handleSearchAction());

        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        View btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(v -> enableMyLocation());

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        tvResultCount = findViewById(R.id.tvResultCount);
        pbLoading = findViewById(R.id.pbLoading);
        rvResults = findViewById(R.id.rvMapResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MapPlaceAdapter(this, place -> {
            if (mMap != null) {
                LatLng location = new LatLng(place.latitude, place.longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        rvResults.setAdapter(adapter);
    }

    // Hàm gọi khi bấm nút tìm kiếm
    private void handleSearchAction() {
        performSearch();
        hideKeyboard();
    }

    // --- LOGIC FILTER UI ---

    private void setupFilterChips() {
        chipGroupFilter.removeAllViews();

        for (String key : POPULAR_FILTERS) {
            String label = CategoryUtils.getLabel(key);
            addChipToGroup(key, label, false);
        }

        Chip moreChip = new Chip(this);
        moreChip.setText("Thêm...");
        moreChip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        moreChip.setChipStrokeColor(ColorStateList.valueOf(Color.LTGRAY));
        moreChip.setChipStrokeWidth(1f);
        moreChip.setTextColor(Color.BLACK);
        moreChip.setOnClickListener(v -> showFullFilterBottomSheet());
        chipGroupFilter.addView(moreChip);
    }

    private void addChipToGroup(String key, String label, boolean isCheckedInitial) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChecked(isCheckedInitial);

        updateChipStyle(chip, isCheckedInitial);

        // [LOGIC SỬA ĐỔI]: Chỉ lưu state, KHÔNG gọi performSearch()
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateChipStyle(chip, isChecked);

            if (isChecked) {
                currentCategory = key;
            } else {
                if (key.equals(currentCategory)) {
                    currentCategory = null;
                }
            }

            // Đã xóa dòng performSearch() ở đây theo yêu cầu
            Log.d(TAG, "Category selected: " + currentCategory + " (Waiting for search button...)");
        });

        chipGroupFilter.addView(chip);
    }

    private void updateChipStyle(Chip chip, boolean isChecked) {
        if (isChecked) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGeneralText)));
            chip.setTextColor(Color.WHITE);
            chip.setChipStrokeWidth(0f);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
            chip.setTextColor(Color.BLACK);
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.LTGRAY));
            chip.setChipStrokeWidth(1f);
        }
    }

    private void showFullFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_filter_bottom_sheet, null);
        ChipGroup fullListGroup = view.findViewById(R.id.chipGroupFullList);

        for (Map.Entry<String, String> entry : CategoryUtils.CATEGORY_MAP.entrySet()) {
            Chip chip = new Chip(this);
            chip.setText(entry.getValue());
            chip.setCheckable(true);

            if (entry.getKey().equals(currentCategory)) {
                chip.setChecked(true);
            }

            chip.setOnClickListener(v -> {
                // Update State
                currentCategory = entry.getKey();
                refreshHorizontalChips(entry.getKey(), entry.getValue());

                // KHÔNG gọi performSearch() -> Chờ user bấm nút search

                bottomSheetDialog.dismiss();
            });

            fullListGroup.addView(chip);
        }
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void refreshHorizontalChips(String key, String label) {
        chipGroupFilter.removeAllViews();
        addChipToGroup(key, label, true);
        for (String popKey : POPULAR_FILTERS) {
            if (!popKey.equals(key)) {
                addChipToGroup(popKey, CategoryUtils.getLabel(popKey), false);
            }
        }
        Chip moreChip = new Chip(this);
        moreChip.setText("Thêm...");
        moreChip.setOnClickListener(v -> showFullFilterBottomSheet());
        chipGroupFilter.addView(moreChip);
    }

    // --- CÁC HÀM TIỆN ÍCH ---

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showLoading() {
        runOnUiThread(() -> {
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
            if (rvResults != null) rvResults.setVisibility(View.GONE);
            if (tvResultCount != null) tvResultCount.setText("Đang tìm...");
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
            if (rvResults != null) rvResults.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        enableMyLocation();

        mMap.setOnMarkerClickListener(marker -> {
            Place place = markerMap.get(marker.getId());
            if (place != null) marker.showInfoWindow();
            return false;
        });

        mMap.setOnMapClickListener(latLng -> {
            if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideKeyboard();
        });

        // [LOGIC SỬA ĐỔI]: Lần đầu vào thì gọi GET ALL, không gọi Search
        loadAllPlaces();
    }

    // --- API 1: LOAD ALL PLACES (Initial) ---
    private void loadAllPlaces() {
        Log.d(TAG, "Initial Load: Get All Places");
        showLoading();
        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading();
                updateMapAndList(data);
                if (data == null || data.isEmpty()) {
                    Toast.makeText(MapActivity.this, "Chưa có địa điểm nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                hideLoading();
                Toast.makeText(MapActivity.this, "Lỗi tải dữ liệu: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- API 2: SEARCH NEARBY (Khi bấm Search) ---
    private void performSearch() {
        if (mMap == null) return;

        double lat, lng;
        if (currentUserLocation != null) {
            lat = currentUserLocation.getLatitude();
            lng = currentUserLocation.getLongitude();
        } else {
            LatLng center = mMap.getCameraPosition().target;
            lat = center.latitude;
            lng = center.longitude;
        }

        // Luôn lấy text mới nhất từ ô nhập
        if (etSearch != null) {
            currentKeyword = etSearch.getText().toString().trim();
        } else {
            currentKeyword = "";
        }

        // Nếu keyword rỗng truyền "" thay vì null
        String nameQuery = currentKeyword.isEmpty() ? "" : currentKeyword;
        String catQuery = currentCategory;

        Log.d(TAG, "Performing Search -> Name: '" + nameQuery + "', Category: " + catQuery);
        showLoading();

        placeRepository.searchNearby(lat, lng, 10, nameQuery, catQuery, new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading();
                updateMapAndList(data);

                if (data == null || data.isEmpty()) {
                    Toast.makeText(MapActivity.this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onError(String message) {
                hideLoading();
                Toast.makeText(MapActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapAndList(List<Place> places) {
        runOnUiThread(() -> {
            if (places == null) return;
            if (adapter != null) adapter.setData(places);
            if (tvResultCount != null) tvResultCount.setText("Kết quả (" + places.size() + ")");
            if (rvResults != null) rvResults.scrollToPosition(0);

            if (mMap != null) {
                mMap.clear();
                markerMap.clear();
                for (Place place : places) {
                    LatLng loc = new LatLng(place.latitude, place.longitude);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(loc).title(place.name).snippet(place.address));
                    if (marker != null) markerMap.put(marker.getId(), place);
                }
            }
        });
    }

    // --- LOCATION ---
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) updateUserLocation(location);
            else requestCurrentLocation();
        });
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        CancellationTokenSource tokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) updateUserLocation(location);
                });
    }

    private void updateUserLocation(Location location) {
        this.currentUserLocation = location;
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }
}