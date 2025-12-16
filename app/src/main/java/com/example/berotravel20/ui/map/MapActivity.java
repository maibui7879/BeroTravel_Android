package com.example.berotravel20.ui.map;

import android.content.Context;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.DirectionStepAdapter;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.ORS.Step;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceResponse;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.data.repository.RouteRepository;
import com.example.berotravel20.utils.CategoryUtils;
import com.example.berotravel20.utils.MapUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapNavigationManager.NavigationListener {

    private static final String TAG = "MapActivity";

    // --- 1. MANAGERS & REPO ---
    private MapHelper mapHelper;
    private LocationHelper locationHelper;
    private MapNavigationManager navigationManager;
    private PlaceRepository placeRepository;
    private RouteRepository routeRepository;

    // --- 2. UI COMPONENTS ---
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private EditText etSearch;
    private ProgressBar pbLoading;
    private ChipGroup chipGroupFilter;
    private RelativeLayout groupMainUI;

    // UI Search Result
    private LinearLayout layoutSearchResults;
    private TextView tvResultCount;
    private MapPlaceAdapter placeAdapter;
    private RecyclerView rvMapResults;
    private MaterialButton btnLoadMore;

    // UI Direction
    private LinearLayout layoutDirections;
    private DirectionStepAdapter stepAdapter;
    private TextView tvDuration, tvDistance;
    private MaterialButtonToggleGroup toggleTransportMode;
    private RelativeLayout layoutNavigationActive;
    private TextView navInstruction, navDistance;
    private ImageView navIcon;

    // --- 3. STATE ---
    private Location currentUserLocation;
    private String currentKeyword = "";
    private String currentCategory = null;
    private int currentRadius = 5;
    private int tempSelectedRadius = 5;

    // Pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoadingMore = false;

    // Navigation
    private Place currentDestination;
    private String currentTransportProfile = "driving-car";
    private boolean isNavigating = false;
    private boolean isFetchingRoute = false;

    private final int[] RADIUS_OPTIONS = {1, 2, 5, 10, 15, 20, 30, 50};
    private final String[] POPULAR_FILTERS = {"restaurant", "cafe", "hotel", "atm", "gas_station", "hospital"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapHelper = new MapHelper(this);
        locationHelper = new LocationHelper(this);
        navigationManager = new MapNavigationManager(this);
        placeRepository = new PlaceRepository();
        routeRepository = new RouteRepository();

        initViews();
        setupFilterChips();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void initViews() {
        groupMainUI = findViewById(R.id.groupMainUI);

        // Search Input
        etSearch = findViewById(R.id.etSearchMap);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch();
                MapUtils.hideKeyboard(this);
                return true;
            }
            return false;
        });
        findViewById(R.id.btnSearchIcon).setOnClickListener(v -> {
            performSearch();
            MapUtils.hideKeyboard(this);
        });

        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (currentUserLocation != null && mapHelper != null)
                mapHelper.moveCamera(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 15f);
            else checkAndGetLocation();
        });

        // Bottom Sheet
        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setPeekHeight(MapUtils.dpToPx(this, 240));

        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(0.5f);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Lists
        layoutSearchResults = findViewById(R.id.layout_search_results);
        pbLoading = findViewById(R.id.pbLoading);
        tvResultCount = findViewById(R.id.tvResultCount);

        rvMapResults = findViewById(R.id.rvMapResults);
        rvMapResults.setLayoutManager(new LinearLayoutManager(this));

        placeAdapter = new MapPlaceAdapter(this, new MapPlaceAdapter.OnItemClickListener() {
            @Override public void onItemClick(Place place) {
                mapHelper.moveCamera(new LatLng(place.latitude, place.longitude), 16f);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            @Override public void onDirectionClick(Place place) { enterPreviewDirectionMode(place); }
        });
        rvMapResults.setAdapter(placeAdapter);

        // Load More Button
        btnLoadMore = findViewById(R.id.btnLoadMore);
        btnLoadMore.setOnClickListener(v -> loadMorePlaces());
        btnLoadMore.setVisibility(View.GONE);

        setupDirectionUI();
    }

    private void setupDirectionUI() {
        layoutDirections = findViewById(R.id.layout_directions);
        tvDuration = findViewById(R.id.tvDuration);
        tvDistance = findViewById(R.id.tvDistance);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        toggleTransportMode = findViewById(R.id.toggleTransportMode);
        toggleTransportMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnModeCar) selectTransportMode("driving-car");
                else if (checkedId == R.id.btnModeBike) selectTransportMode("cycling-regular");
                else if (checkedId == R.id.btnModeWalk) selectTransportMode("foot-walking");
            }
        });

        findViewById(R.id.btnExitDirection).setOnClickListener(v -> exitPreviewDirectionMode());
        findViewById(R.id.btnStartNavigation).setOnClickListener(v -> startNavigation());

        RecyclerView rvSteps = findViewById(R.id.rvDirectionSteps);
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        stepAdapter = new DirectionStepAdapter();
        rvSteps.setAdapter(stepAdapter);

        layoutNavigationActive = findViewById(R.id.layoutNavigationActive);
        navInstruction = findViewById(R.id.navStepInstruction);
        navDistance = findViewById(R.id.navStepDistance);
        navIcon = findViewById(R.id.navStepIcon);
        findViewById(R.id.btnStopNavigation).setOnClickListener(v -> stopNavigation());
    }

    // --- LOGIC TÌM KIẾM & LOAD DATA ---

    // 1. Load Mặc định: Gọi getAllPlaces (Bất chấp có Location hay chưa)
    private void loadAllPlaces() {
        // KHÔNG check location ở đây

        // Reset UI
        if (placeAdapter != null) placeAdapter.clearData();
        if (tvResultCount != null) tvResultCount.setText("Đang tải dữ liệu...");
        btnLoadMore.setVisibility(View.GONE);
        showLoading();

        // Gọi API Get All
        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading();
                if (data != null) {
                    // [CHỐT CHẶN AN TOÀN - FIX ANR]
                    // API trả về 7000+ item. Nếu hiển thị hết -> Crash ngay lập tức.
                    // Chỉ lấy 50 item đầu tiên để hiển thị cho mượt.
                    List<Place> safeList = data;
                    if (data.size() > 50) {
                        safeList = data.subList(0, 50);
                    }

                    if (tvResultCount != null) tvResultCount.setText("Khám phá");
                    updateMapAndList(safeList, true);

                    if (!safeList.isEmpty() && currentDestination == null) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            }

            @Override
            public void onError(String message) {
                hideLoading();
                Toast.makeText(MapActivity.this, "Lỗi tải dữ liệu: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 2. Tìm kiếm (Có Location + Validation)
    private void performSearch() {
        if (currentUserLocation == null) { checkAndGetLocation(); return; }

        String inputRaw = "";
        if (etSearch != null) inputRaw = etSearch.getText().toString().trim();

        // Validation
        if (inputRaw.isEmpty() || inputRaw.length() < 2) {
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Vui lòng nhập từ khóa tìm kiếm (ít nhất 2 ký tự).")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }

        currentKeyword = inputRaw;

        // Reset và Gọi API Search Nearby (Fast Mode - Page Null)
        resetAndCallSearchApi(null, null);
    }

    // 3. Tải thêm (Chỉ dùng cho Search Nearby)
    private void loadMorePlaces() {
        if (isLoadingMore) return;
        btnLoadMore.setText("Đang tải...");
        btnLoadMore.setEnabled(false);

        currentPage++;
        callSearchNearbyApi(currentPage, 10);
    }

    // --- HELPER CALL API ---

    private void resetAndCallSearchApi(Integer page, Integer limit) {
        currentPage = 1;
        totalPages = 1;
        isLoadingMore = true;
        if (placeAdapter != null) placeAdapter.clearData();
        if (tvResultCount != null) tvResultCount.setText("Đang tìm...");
        if (currentDestination == null) mapHelper.drawSearchRadius(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), currentRadius);
        btnLoadMore.setVisibility(View.GONE);
        showLoading();

        callSearchNearbyApi(page, limit);
    }

    private void callSearchNearbyApi(Integer pageToLoad, Integer limit) {
        isLoadingMore = true;
        if (pageToLoad == null || pageToLoad == 1) showLoading();

        placeRepository.searchNearby(
                currentUserLocation.getLatitude(),
                currentUserLocation.getLongitude(),
                currentRadius,
                currentKeyword,
                currentCategory,
                pageToLoad,
                limit,
                new DataCallback<PlaceResponse>() {
                    @Override
                    public void onSuccess(PlaceResponse response) {
                        hideLoading();
                        isLoadingMore = false;
                        btnLoadMore.setEnabled(true);
                        btnLoadMore.setText("Xem thêm kết quả");

                        if (response != null && response.data != null) {
                            handleSearchResponse(response, pageToLoad);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        hideLoading();
                        isLoadingMore = false;
                        btnLoadMore.setEnabled(true);
                        btnLoadMore.setText("Thử lại");
                        btnLoadMore.setVisibility(View.VISIBLE);
                        if (pageToLoad != null && pageToLoad > 1) currentPage--;
                        Toast.makeText(MapActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSearchResponse(PlaceResponse response, Integer pageToLoad) {
        boolean hasNextPage = false;

        // Fast Mode (Lần đầu)
        if (pageToLoad == null || pageToLoad == 1) {
            if (response.data.size() >= 10) {
                totalPages = 2;
                hasNextPage = true;
            } else {
                totalPages = 1;
                hasNextPage = false;
            }
            if (tvResultCount != null) tvResultCount.setText(hasNextPage ? "Kết quả (10+)" : "Kết quả (" + response.data.size() + ")");
            updateMapAndList(response.data, true);
            if (!response.data.isEmpty() && currentDestination == null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        // Paging Mode (Load more)
        else {
            if (response.totalPages > 0) totalPages = response.totalPages;
            if (tvResultCount != null) tvResultCount.setText("Kết quả (" + response.total + ")");
            hasNextPage = (currentPage < totalPages);
            updateMapAndList(response.data, false);
        }

        if (hasNextPage) btnLoadMore.setVisibility(View.VISIBLE);
        else btnLoadMore.setVisibility(View.GONE);
    }

    private void updateMapAndList(List<Place> places, boolean isReset) {
        runOnUiThread(() -> {
            if (placeAdapter != null) {
                if (isReset) {
                    placeAdapter.setData(places);
                    mapHelper.showMarkers(places);
                } else {
                    placeAdapter.addData(places);
                }
            }
        });
    }

    // --- MAP & LOCATION LIFECYCLE ---

    @Override
    public void onMapReady(@NonNull GoogleMap g) {
        mapHelper.setGoogleMap(g);
        g.setOnMapClickListener(l -> {
            if (!isNavigating) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            MapUtils.hideKeyboard(this);
        });

        // 1. Gọi GetAll ngay lập tức (Theo yêu cầu)
        loadAllPlaces();

        // 2. Lấy vị trí song song (Chỉ để hiện Blue Dot, không gọi API nữa)
        checkAndGetLocation();
    }

    private void checkAndGetLocation() {
        locationHelper.getLastLocation(l -> {
            currentUserLocation = l;
            // Chỉ move camera nếu chưa chọn địa điểm nào
            if (currentDestination == null) mapHelper.moveCamera(new LatLng(l.getLatitude(), l.getLongitude()), 15f);
            if (locationHelper.hasPermission()) mapHelper.enableMyLocationLayer();
        });
    }

    // --- NAVIGATION LISTENER ---
    @Override public void onUpdateInstruction(String i, String d) { runOnUiThread(() -> { navInstruction.setText(i); navDistance.setText(d); }); }
    @Override public void onNextStep(String i) { runOnUiThread(() -> Toast.makeText(this, "Tiếp: " + i, Toast.LENGTH_SHORT).show()); }
    @Override public void onArrived() { runOnUiThread(() -> { Toast.makeText(this, "Đến nơi!", Toast.LENGTH_LONG).show(); stopNavigation(); }); }
    @Override public void onRerouteNeeded() { runOnUiThread(() -> { if (!isFetchingRoute) { Toast.makeText(this, "Đang định tuyến lại...", Toast.LENGTH_SHORT).show(); fetchRoute(currentDestination); } }); }

    // --- HELPERS (Start/Stop Nav, Route, Chips, Utils...) ---
    private void startNavigation() { if(currentUserLocation==null)return; isNavigating=true; bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN); groupMainUI.setVisibility(View.GONE); layoutNavigationActive.setVisibility(View.VISIBLE); mapHelper.disableMyLocationLayer(); mapHelper.updateNavigationCamera(currentUserLocation); locationHelper.startLocationUpdates(l->{currentUserLocation=l; if(isNavigating){mapHelper.updateNavigationCamera(l); navigationManager.onLocationUpdated(l);}}); }
    private void stopNavigation() { isNavigating=false; locationHelper.stopLocationUpdates(); layoutNavigationActive.setVisibility(View.GONE); groupMainUI.setVisibility(View.VISIBLE); bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED); mapHelper.stopNavigationMode(); mapHelper.enableMyLocationLayer(); }
    private void fetchRoute(Place d) { if(currentUserLocation==null||isFetchingRoute)return; isFetchingRoute=true; tvDuration.setText("..."); routeRepository.getRoute(currentTransportProfile, currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), d.latitude, d.longitude, new RouteRepository.RouteCallback(){@Override public void onSuccess(List<LatLng> p, List<Step> s, double d, double du) { isFetchingRoute=false; mapHelper.drawPolyline(p); navigationManager.startNewRoute(s, p); if(d<1000) tvDistance.setText((int)d+" m"); else tvDistance.setText(String.format("%.1f km",d/1000)); tvDuration.setText((int)(du/60)+" phút"); if(stepAdapter!=null) stepAdapter.setData(s); } @Override public void onError(String m) { isFetchingRoute=false; tvDuration.setText("Lỗi"); }}); }

    private void selectTransportMode(String m) { if (!m.equals(currentTransportProfile)) { currentTransportProfile = m; if (currentDestination != null) fetchRoute(currentDestination); } }
    private void enterPreviewDirectionMode(Place p) { currentDestination = p; layoutSearchResults.setVisibility(View.GONE); layoutDirections.setVisibility(View.VISIBLE); bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED); currentTransportProfile = "driving-car"; toggleTransportMode.check(R.id.btnModeCar); fetchRoute(p); }
    private void exitPreviewDirectionMode() { mapHelper.clearPolyline(); layoutDirections.setVisibility(View.GONE); layoutSearchResults.setVisibility(View.VISIBLE); bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); currentDestination = null; if (currentUserLocation != null) mapHelper.moveCamera(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 15f); }
    private void showLoading() { if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE); }
    private void hideLoading() { if (pbLoading != null) pbLoading.setVisibility(View.GONE); }

    // Chips
    private void setupFilterChips() { chipGroupFilter.removeAllViews(); addRadiusChip(); for (String k : POPULAR_FILTERS) addCategoryChip(k, CategoryUtils.getLabel(k)); Chip m = new Chip(this); m.setText("Thêm..."); setChipStyle(m, false); m.setOnClickListener(v -> showFullFilterBottomSheet()); chipGroupFilter.addView(m); }
    private void addRadiusChip() { Chip c = new Chip(this); c.setText("Trong " + currentRadius + " km"); c.setChipIconResource(R.drawable.ic_search_gray); setChipStyle(c, false); c.setOnClickListener(v -> showRadiusSelectionDialog(c)); chipGroupFilter.addView(c); }
    private void addCategoryChip(String k, String l) { Chip c = new Chip(this); c.setText(l); c.setCheckable(true); setChipStyle(c, k.equals(currentCategory)); c.setOnCheckedChangeListener((v, b) -> { setChipStyle(c, b); if (b) currentCategory = k; else if (k.equals(currentCategory)) currentCategory = null; }); chipGroupFilter.addView(c); }
    private void showRadiusSelectionDialog(Chip u) { BottomSheetDialog d = new BottomSheetDialog(this); View v = LayoutInflater.from(this).inflate(R.layout.layout_radius_bottom_sheet, null); ChipGroup g = v.findViewById(R.id.cgRadiusPresets); v.findViewById(R.id.btnApplyRadius).setOnClickListener(view -> { currentRadius = tempSelectedRadius; u.setText("Trong " + currentRadius + " km"); d.dismiss(); }); tempSelectedRadius = currentRadius; for (int km : RADIUS_OPTIONS) { Chip c = new Chip(this); c.setText(km + " km"); c.setCheckable(true); boolean s = (km == currentRadius); setChipStyle(c, s); if (s) c.setChecked(true); c.setOnCheckedChangeListener((view, isc) -> { if (isc) { tempSelectedRadius = km; setChipStyle(c, true); } else setChipStyle(c, false); }); g.addView(c); } d.setContentView(v); d.show(); }
    private void setChipStyle(Chip c, boolean s) { if (s) { c.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryBackground))); c.setTextColor(ContextCompat.getColor(this, R.color.white)); c.setChipStrokeWidth(0f); } else { c.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))); c.setTextColor(ContextCompat.getColor(this, R.color.colorGeneralText)); c.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_gray_stroke))); c.setChipStrokeWidth(1f); } }
    private void showFullFilterBottomSheet() { BottomSheetDialog d = new BottomSheetDialog(this); View v = LayoutInflater.from(this).inflate(R.layout.layout_filter_bottom_sheet, null); ChipGroup g = v.findViewById(R.id.chipGroupFullList); for (java.util.Map.Entry<String, String> e : CategoryUtils.CATEGORY_MAP.entrySet()) { Chip c = new Chip(this); c.setText(e.getValue()); c.setCheckable(true); boolean s = e.getKey().equals(currentCategory); if (s) c.setChecked(true); setChipStyle(c, s); c.setOnClickListener(view -> { currentCategory = e.getKey(); setupFilterChips(); d.dismiss(); }); g.addView(c); } d.setContentView(v); d.show(); }

    @Override public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) { super.onRequestPermissionsResult(r, p, g); if (r == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE && g.length > 0) checkAndGetLocation(); }
}