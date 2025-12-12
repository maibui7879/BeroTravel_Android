package com.example.berotravel20.ui.map;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.data.repository.RouteRepository;
import com.example.berotravel20.utils.CategoryUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Helper & Repository
    private MapHelper mapHelper;
    private LocationHelper locationHelper;
    private PlaceRepository placeRepository;
    private RouteRepository routeRepository;

    // UI Components
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private MapPlaceAdapter adapter;
    private EditText etSearch;
    private TextView tvResultCount;
    private ProgressBar pbLoading;
    private ChipGroup chipGroupFilter;

    // State
    private Location currentUserLocation;
    private String currentKeyword = "";
    private String currentCategory = null;
    private int currentRadius = 5;
    private int tempSelectedRadius = 5;

    private final int[] RADIUS_OPTIONS = {1, 2, 5, 10, 15, 20, 30, 50};
    private final String[] POPULAR_FILTERS = {"restaurant", "cafe", "hotel", "atm", "gas_station", "hospital"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Init Helpers & Repos
        mapHelper = new MapHelper();
        locationHelper = new LocationHelper(this);
        placeRepository = new PlaceRepository();
        routeRepository = new RouteRepository();

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
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                handleSearchAction();
                return true;
            }
            return false;
        });

        findViewById(R.id.btnSearchIcon).setOnClickListener(v -> handleSearchAction());
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (currentUserLocation != null) mapHelper.moveCamera(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 15f);
            else checkAndGetLocation();
        });

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        tvResultCount = findViewById(R.id.tvResultCount);
        pbLoading = findViewById(R.id.pbLoading);
        RecyclerView rvResults = findViewById(R.id.rvMapResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // Init Adapter
        adapter = new MapPlaceAdapter(this, new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Place place) {
                mapHelper.moveCamera(new LatLng(place.latitude, place.longitude), 16f);
                if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onDirectionClick(Place place) {
                fetchAndDrawRoute(place);
            }
        });
        rvResults.setAdapter(adapter);
    }

    private void handleSearchAction() {
        performSearch();
        hideKeyboard();
    }

    // --- FILTERS UI ---
    private void setupFilterChips() {
        chipGroupFilter.removeAllViews();
        addRadiusChip();
        for (String key : POPULAR_FILTERS) {
            addCategoryChip(key, CategoryUtils.getLabel(key));
        }
        Chip moreChip = new Chip(this);
        moreChip.setText("Thêm...");
        // Style cho nút "Thêm..." (Luôn ở trạng thái chưa chọn)
        setChipStyle(moreChip, false);
        moreChip.setOnClickListener(v -> showFullFilterBottomSheet());
        chipGroupFilter.addView(moreChip);
    }

    private void addRadiusChip() {
        Chip radiusChip = new Chip(this);
        radiusChip.setText("Trong " + currentRadius + " km");
        radiusChip.setChipIconResource(R.drawable.ic_search_gray);

        // [CẬP NHẬT] Sử dụng hàm setChipStyle để áp dụng màu
        setChipStyle(radiusChip, false); // Mặc định là style chưa chọn

        radiusChip.setOnClickListener(v -> showRadiusSelectionDialog(radiusChip));
        chipGroupFilter.addView(radiusChip);
    }

    private void showRadiusSelectionDialog(Chip chipToUpdate) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_radius_bottom_sheet, null);
        ChipGroup cgRadius = view.findViewById(R.id.cgRadiusPresets);
        Button btnApply = view.findViewById(R.id.btnApplyRadius);

        // [CẬP NHẬT] Màu cho nút Áp dụng
        btnApply.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryBackground)));
        btnApply.setTextColor(ContextCompat.getColor(this, R.color.white));

        tempSelectedRadius = currentRadius;

        for (int km : RADIUS_OPTIONS) {
            Chip choiceChip = new Chip(this);
            choiceChip.setText(km + " km");
            choiceChip.setCheckable(true);
            choiceChip.setClickable(true);

            // [CẬP NHẬT] Set style dựa trên việc nó có đang được chọn hay không
            boolean isSelected = (km == currentRadius);
            setChipStyle(choiceChip, isSelected);
            if (isSelected) choiceChip.setChecked(true);

            choiceChip.setOnCheckedChangeListener((v, isChecked) -> {
                if (isChecked) {
                    tempSelectedRadius = km;
                    setChipStyle(choiceChip, true);
                } else {
                    setChipStyle(choiceChip, false);
                }
            });
            cgRadius.addView(choiceChip);
        }
        btnApply.setOnClickListener(v -> {
            currentRadius = tempSelectedRadius;
            chipToUpdate.setText("Trong " + currentRadius + " km");
            // Có thể muốn set style active cho chip Radius bên ngoài sau khi chọn
            // setChipStyle(chipToUpdate, true);
            dialog.dismiss();
        });
        dialog.setContentView(view);
        dialog.show();
    }

    private void addCategoryChip(String key, String label) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);

        // [CẬP NHẬT] Set style ban đầu
        setChipStyle(chip, key.equals(currentCategory));

        chip.setOnCheckedChangeListener((v, isChecked) -> {
            // [CẬP NHẬT] Đổi style khi check/uncheck
            setChipStyle(chip, isChecked);
            if (isChecked) currentCategory = key;
            else if (key.equals(currentCategory)) currentCategory = null;
        });
        chipGroupFilter.addView(chip);
    }

    // [THÊM MỚI] Hàm quản lý màu sắc trung tâm cho Chip
    private void setChipStyle(Chip chip, boolean isSelected) {
        if (isSelected) {
            // Đang chọn: Nền màu chủ đạo, Chữ trắng, Không viền, Icon trắng
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryBackground)));
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            chip.setChipStrokeWidth(0f);
            if (chip.getChipIcon() != null) {
                chip.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            }
        } else {
            // Không chọn: Nền trắng, Chữ màu General, Viền xám nhạt, Icon màu General
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            chip.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryText));
            chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_gray_stroke)));
            chip.setChipStrokeWidth(1f); // Lưu ý: 1f ở đây là pixel, chuẩn hơn nên dùng dp convert sang px
            if (chip.getChipIcon() != null) {
                chip.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryText)));
            }
        }
    }

    private void showFullFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_filter_bottom_sheet, null);
        ChipGroup fullListGroup = view.findViewById(R.id.chipGroupFullList);
        for (java.util.Map.Entry<String, String> entry : CategoryUtils.CATEGORY_MAP.entrySet()) {
            Chip chip = new Chip(this);
            chip.setText(entry.getValue());
            chip.setCheckable(true);

            // [CẬP NHẬT] Set style
            boolean isSelected = entry.getKey().equals(currentCategory);
            if (isSelected) chip.setChecked(true);
            setChipStyle(chip, isSelected);

            chip.setOnClickListener(v -> {
                currentCategory = entry.getKey();
                setupFilterChips(); // Refresh UI
                dialog.dismiss();
            });
            fullListGroup.addView(chip);
        }
        dialog.setContentView(view);
        dialog.show();
    }

    // --- MAP & DATA LOGIC ---

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapHelper.setGoogleMap(googleMap);

        googleMap.setOnMarkerClickListener(marker -> {
            Place place = mapHelper.getPlaceByMarkerId(marker.getId());
            if (place != null) marker.showInfoWindow();
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> {
            if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideKeyboard();
        });

        checkAndGetLocation();
        loadAllPlaces();
    }

    private void checkAndGetLocation() {
        locationHelper.getLastLocation(location -> {
            currentUserLocation = location;
            mapHelper.moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), 15f);
            // Note: Việc bật myLocationEnabled cần được xử lý cẩn thận khi tách code.
            // Nếu LocationHelper đã check quyền, ta có thể báo cho MapHelper bật nó lên.
        });
    }

    // --- API CALLS ---

    private void loadAllPlaces() {
        showLoading();
        mapHelper.clearPolyline();
        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading();
                updateUI(data);
            }
            @Override public void onError(String message) { hideLoading(); }
        });
    }

    private void performSearch() {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();
            checkAndGetLocation();
            return;
        }

        if (etSearch != null) currentKeyword = etSearch.getText().toString().trim();

        // Vẽ vòng tròn (Màu sắc được xử lý bên trong MapHelper)
        mapHelper.drawSearchRadius(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), currentRadius);

        showLoading();
        placeRepository.searchNearby(
                currentUserLocation.getLatitude(),
                currentUserLocation.getLongitude(),
                currentRadius,
                currentKeyword.isEmpty() ? "" : currentKeyword,
                currentCategory,
                new DataCallback<List<Place>>() {
                    @Override
                    public void onSuccess(List<Place> data) {
                        hideLoading();
                        updateUI(data);
                        if (data != null && !data.isEmpty()) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        } else {
                            Toast.makeText(MapActivity.this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onError(String message) { hideLoading(); }
                }
        );
    }

    private void fetchAndDrawRoute(Place destination) {
        if (currentUserLocation == null) {
            checkAndGetLocation();
            Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đang tìm đường...", Toast.LENGTH_SHORT).show();

        routeRepository.getRoute(
                currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                destination.latitude, destination.longitude,
                new RouteRepository.RouteCallback() {
                    @Override
                    public void onSuccess(List<LatLng> path) {
                        // Vẽ đường (Màu sắc được xử lý bên trong MapHelper)
                        mapHelper.drawPolyline(path);
                        if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(MapActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void updateUI(List<Place> places) {
        runOnUiThread(() -> {
            if (places == null) return;
            if (adapter != null) adapter.setData(places);
            if (tvResultCount != null) tvResultCount.setText("Kết quả (" + places.size() + ")");
            mapHelper.showMarkers(places);
        });
    }

    // --- UTILS ---
    private void showLoading() { if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE); }
    private void hideLoading() { if (pbLoading != null) pbLoading.setVisibility(View.GONE); }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            checkAndGetLocation();
        }
    }
}