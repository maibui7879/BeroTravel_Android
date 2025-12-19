package com.example.berotravel20.ui.map;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.DirectionStepAdapter;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.Favorite.FavoriteResponse;
import com.example.berotravel20.data.model.ORS.Step;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceResponse;
import com.example.berotravel20.data.repository.FavoriteRepository;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.data.repository.RouteRepository;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.common.RequestLoginDialog;
import com.example.berotravel20.utils.CategoryUtils;
import com.example.berotravel20.utils.MapUtils;
import com.example.berotravel20.utils.ToastUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        MapNavigationManager.NavigationListener,
        RequestLoginDialog.RequestLoginListener {

    private static final String TAG = "MapActivity";

    // --- 1. MANAGERS & REPO ---
    private MapHelper mapHelper;
    private LocationHelper locationHelper;
    private MapNavigationManager navigationManager;
    private PlaceRepository placeRepository;
    private RouteRepository routeRepository;
    private FavoriteRepository favoriteRepository;

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

        // Init
        mapHelper = new MapHelper(this);
        locationHelper = new LocationHelper(this);
        navigationManager = new MapNavigationManager(this);
        placeRepository = new PlaceRepository();
        routeRepository = new RouteRepository();
        favoriteRepository = new FavoriteRepository();

        initViews();
        setupFilterChips();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Load Favorites
        fetchMyFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMyFavorites();
    }

    private void initViews() {
        groupMainUI = findViewById(R.id.groupMainUI);

        // Xử lý Nút Back
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

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

        // Setup Adapter
        placeAdapter = new MapPlaceAdapter(this, new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onFavoriteClick(Place place) {
                handleFavoriteToggle(place);
            }
        });
        rvMapResults.setAdapter(placeAdapter);

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

    // --- LOGIC FAVORITE ---

    private void fetchMyFavorites() {
        if (isUserLoggedIn()) {
            favoriteRepository.getMyFavorites(new DataCallback<List<Place>>() {
                @Override
                public void onSuccess(List<Place> places) {
                    if (placeAdapter != null) {
                        List<String> ids = new ArrayList<>();
                        if (places != null) {
                            for (Place p : places) {
                                if (p.id != null) ids.add(p.id);
                            }
                        }
                        placeAdapter.setFavoriteIds(ids);
                    }
                }
                @Override
                public void onError(String message) {}
            });
        } else {
            if (placeAdapter != null) placeAdapter.setFavoriteIds(null);
        }
    }

    private void handleFavoriteToggle(Place place) {
        if (!isUserLoggedIn()) {
            showLoginRequestDialog();
            return;
        }

        favoriteRepository.toggleFavorite(place.id, new DataCallback<FavoriteResponse>() {
            @Override
            public void onSuccess(FavoriteResponse data) {
                ToastUtils.showSuccess(MapActivity.this, data.message);
                if (placeAdapter != null) placeAdapter.toggleFavoriteLocal(place.id);
            }

            @Override
            public void onError(String message) {
                ToastUtils.showError(MapActivity.this, message);
            }
        });
    }

    private boolean isUserLoggedIn() {
        String token = TokenManager.getInstance(this).getToken();
        return token != null && !token.isEmpty();
    }

    private void showLoginRequestDialog() {
        RequestLoginDialog dialog = RequestLoginDialog.newInstance();
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "RequestLoginDialog");
    }

    @Override
    public void onLoginClick() {
        startActivity(new Intent(this, AuthActivity.class));
    }

    @Override
    public void onCancelClick() {}

    // --- SEARCH & MAP LOGIC ---

    private void loadAllPlaces() {
        if (placeAdapter != null) placeAdapter.clearData();
        if (tvResultCount != null) tvResultCount.setText("Đang tải dữ liệu...");
        btnLoadMore.setVisibility(View.GONE);
        showLoading();

        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading();
                if (data != null) {
                    List<Place> safeList = data.size() > 50 ? data.subList(0, 50) : data;
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
                ToastUtils.showError(MapActivity.this, message);
            }
        });
    }

    private void performSearch() {
        if (currentUserLocation == null) { checkAndGetLocation(); return; }
        String inputRaw = etSearch != null ? etSearch.getText().toString().trim() : "";
        if (inputRaw.length() < 2) {
            ToastUtils.showWarning(this, "Vui lòng nhập ít nhất 2 ký tự");
            return;
        }
        currentKeyword = inputRaw;
        resetAndCallSearchApi(null, null);
    }

    private void loadMorePlaces() {
        if (isLoadingMore) return;
        btnLoadMore.setText("Đang tải...");
        btnLoadMore.setEnabled(false);
        currentPage++;
        callSearchNearbyApi(currentPage, 10);
    }

    private void resetAndCallSearchApi(Integer page, Integer limit) {
        currentPage = 1; totalPages = 1; isLoadingMore = true;
        if (placeAdapter != null) placeAdapter.clearData();
        if (tvResultCount != null) tvResultCount.setText("Đang tìm...");
        if (currentDestination == null && currentUserLocation != null)
            mapHelper.drawSearchRadius(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), currentRadius);
        btnLoadMore.setVisibility(View.GONE);
        showLoading();
        callSearchNearbyApi(page, limit);
    }

    private void callSearchNearbyApi(Integer pageToLoad, Integer limit) {
        isLoadingMore = true;
        if (pageToLoad == null || pageToLoad == 1) showLoading();

        placeRepository.searchNearby(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), currentRadius, currentKeyword, currentCategory, pageToLoad, limit,
                new DataCallback<PlaceResponse>() {
                    @Override
                    public void onSuccess(PlaceResponse response) {
                        hideLoading();
                        isLoadingMore = false;
                        btnLoadMore.setEnabled(true);
                        btnLoadMore.setText("Xem thêm kết quả");
                        if (response != null && response.data != null) handleSearchResponse(response, pageToLoad);
                    }
                    @Override
                    public void onError(String message) {
                        hideLoading();
                        isLoadingMore = false;
                        btnLoadMore.setEnabled(true);
                        btnLoadMore.setText("Thử lại");
                        btnLoadMore.setVisibility(View.VISIBLE);
                        if (pageToLoad != null && pageToLoad > 1) currentPage--;
                        ToastUtils.showError(MapActivity.this, "Lỗi: " + message);
                    }
                });
    }

    private void handleSearchResponse(PlaceResponse response, Integer pageToLoad) {
        boolean hasNextPage = false;
        if (pageToLoad == null || pageToLoad == 1) {
            hasNextPage = response.data.size() >= 10;
            totalPages = hasNextPage ? 2 : 1;
            if (tvResultCount != null) tvResultCount.setText(hasNextPage ? "Kết quả (10+)" : "Kết quả (" + response.data.size() + ")");
            updateMapAndList(response.data, true);
            if (!response.data.isEmpty() && currentDestination == null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if (response.totalPages > 0) totalPages = response.totalPages;
            if (tvResultCount != null) tvResultCount.setText("Kết quả (" + response.total + ")");
            hasNextPage = (currentPage < totalPages);
            updateMapAndList(response.data, false);
        }
        btnLoadMore.setVisibility(hasNextPage ? View.VISIBLE : View.GONE);
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

    @Override
    public void onMapReady(@NonNull GoogleMap g) {
        mapHelper.setGoogleMap(g);
        g.setOnMapClickListener(l -> {
            if (!isNavigating) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            MapUtils.hideKeyboard(this);
        });

        // 1. Kiểm tra Intent (Search hoặc Navigation)
        handleIntentData();

        // 2. Nếu không có Search Keyword thì mới load All Places mặc định
        if (currentKeyword.isEmpty() && currentDestination == null) {
            loadAllPlaces();
        }

        // 3. Lấy vị trí -> Nếu có Search Keyword thì sẽ trigger search trong callback của checkAndGetLocation
        checkAndGetLocation();
    }

    // [CẬP NHẬT] Hàm xử lý Intent đầu vào
    private void handleIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        // Case A: Direct Navigation (Chỉ đường tới 1 điểm cụ thể)
        if ("DIRECT_TO_PLACE".equals(intent.getStringExtra("ACTION_TYPE"))) {
            double lat = intent.getDoubleExtra("TARGET_LAT", 0);
            double lng = intent.getDoubleExtra("TARGET_LNG", 0);
            String name = intent.getStringExtra("TARGET_NAME");

            if (lat != 0 && lng != 0) {
                Place p = new Place();
                p.latitude = lat; p.longitude = lng; p.name = name;
                enterPreviewDirectionMode(p);
            }
        }

        // Case B: Search Query (Từ HomeFragment bấm vào search bar)
        if (intent.hasExtra("SEARCH_QUERY")) {
            String query = intent.getStringExtra("SEARCH_QUERY");
            if (query != null && !query.isEmpty()) {
                currentKeyword = query;
                etSearch.setText(query); // Điền text vào ô search
                // Chưa gọi search API vội, đợi có vị trí ở checkAndGetLocation
            }
        }
    }

    // [CẬP NHẬT] Hàm lấy vị trí và điều phối luồng tiếp theo
    private void checkAndGetLocation() {
        locationHelper.getLastLocation(l -> {
            currentUserLocation = l;
            if (l == null) return;

            // Di chuyển camera về vị trí người dùng (nếu chưa chọn đích)
            if (currentDestination == null) {
                mapHelper.moveCamera(new LatLng(l.getLatitude(), l.getLongitude()), 15f);
            }
            if (locationHelper.hasPermission()) mapHelper.enableMyLocationLayer();

            // LOGIC ĐIỀU PHỐI:
            if (!currentKeyword.isEmpty()) {
                // Ưu tiên 1: Có từ khóa tìm kiếm -> Gọi Search API ngay
                resetAndCallSearchApi(null, null);
            } else if (currentDestination != null && !isFetchingRoute) {
                // Ưu tiên 2: Có điểm đến (Navigation) -> Tìm đường
                fetchRoute(currentDestination);
            }
            // Nếu không có gì đặc biệt thì `loadAllPlaces` đã được gọi ở onMapReady rồi.
        });
    }

    // --- NAVIGATION LOGIC (Giữ nguyên) ---
    @Override public void onUpdateInstruction(String i, String d) { runOnUiThread(() -> { navInstruction.setText(i); navDistance.setText(d); }); }

    @Override public void onNextStep(String i) {
        runOnUiThread(() -> {
            ToastUtils.show(this, "Tiếp: " + i, ToastUtils.INFO);
        });
    }

    @Override public void onArrived() {
        runOnUiThread(() -> {
            ToastUtils.showSuccess(this, "Bạn đã đến nơi!");
            stopNavigation();
        });
    }

    @Override public void onRerouteNeeded() {
        runOnUiThread(() -> {
            ToastUtils.showWarning(this, "Đang định tuyến lại...");
            if (!isFetchingRoute) fetchRoute(currentDestination);
        });
    }

    private void startNavigation() {
        if (currentUserLocation == null) return;
        isNavigating = true;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        groupMainUI.setVisibility(View.GONE);
        layoutNavigationActive.setVisibility(View.VISIBLE);
        mapHelper.disableMyLocationLayer();
        mapHelper.updateNavigationCamera(currentUserLocation);
        locationHelper.startLocationUpdates(l -> {
            currentUserLocation = l;
            if (isNavigating) {
                mapHelper.updateNavigationCamera(l);
                navigationManager.onLocationUpdated(l);
            }
        });
    }

    private void stopNavigation() {
        isNavigating = false;
        locationHelper.stopLocationUpdates();
        layoutNavigationActive.setVisibility(View.GONE);
        groupMainUI.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        mapHelper.stopNavigationMode();
        mapHelper.enableMyLocationLayer();
    }

    private String formatDuration(double durationSeconds) {
        long totalMinutes = (long) (durationSeconds / 60);
        if (totalMinutes < 60) return totalMinutes + " phút";
        else if (totalMinutes < 1440) return (totalMinutes / 60) + " giờ " + (totalMinutes % 60) + " phút";
        else return (totalMinutes / 1440) + " ngày " + ((totalMinutes % 1440) / 60) + " giờ " + ((totalMinutes % 1440) % 60) + " phút";
    }

    private void fetchRoute(Place d) {
        if (currentUserLocation == null || isFetchingRoute) return;
        isFetchingRoute = true;
        tvDuration.setText("...");

        routeRepository.getRoute(currentTransportProfile, currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), d.latitude, d.longitude, new RouteRepository.RouteCallback() {
            @Override
            public void onSuccess(List<LatLng> p, List<Step> s, double dis, double dur) {
                isFetchingRoute = false;
                mapHelper.drawPolyline(p);
                navigationManager.startNewRoute(s, p);
                tvDistance.setText(dis < 1000 ? (int) dis + " m" : String.format("%.1f km", dis / 1000));
                tvDuration.setText(formatDuration(dur));
                if (stepAdapter != null) stepAdapter.setData(s);
            }
            @Override public void onError(String m) {
                isFetchingRoute = false;
                tvDuration.setText("Lỗi");
                ToastUtils.showError(MapActivity.this, "Lỗi tìm đường: " + m);
            }
        });
    }

    private void selectTransportMode(String m) {
        if (!m.equals(currentTransportProfile)) {
            currentTransportProfile = m;
            if (currentDestination != null) fetchRoute(currentDestination);
        }
    }

    private void enterPreviewDirectionMode(Place p) {
        currentDestination = p;
        layoutSearchResults.setVisibility(View.GONE);
        layoutDirections.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        currentTransportProfile = "driving-car";
        toggleTransportMode.check(R.id.btnModeCar);
        mapHelper.clearMarkers();
        mapHelper.addDestinationMarker(p);
        if (currentUserLocation != null) fetchRoute(p);
    }

    private void exitPreviewDirectionMode() {
        mapHelper.clearPolyline();
        layoutDirections.setVisibility(View.GONE);
        layoutSearchResults.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        currentDestination = null;
        if (currentUserLocation != null) mapHelper.moveCamera(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 15f);
        loadAllPlaces();
    }

    private void showLoading() { if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE); }
    private void hideLoading() { if (pbLoading != null) pbLoading.setVisibility(View.GONE); }

    // --- CHIPS ---
    private void setupFilterChips() {
        chipGroupFilter.removeAllViews();
        addRadiusChip();
        for (String k : POPULAR_FILTERS) addCategoryChip(k, CategoryUtils.getLabel(k));
        Chip m = new Chip(this); m.setText("Thêm..."); setChipStyle(m, false); m.setOnClickListener(v -> showFullFilterBottomSheet()); chipGroupFilter.addView(m);
    }
    private void addRadiusChip() { Chip c = new Chip(this); c.setText("Trong " + currentRadius + " km"); c.setChipIconResource(R.drawable.ic_search_gray); setChipStyle(c, false); c.setOnClickListener(v -> showRadiusSelectionDialog(c)); chipGroupFilter.addView(c); }
    private void addCategoryChip(String k, String l) { Chip c = new Chip(this); c.setText(l); c.setCheckable(true); setChipStyle(c, k.equals(currentCategory)); c.setOnCheckedChangeListener((v, b) -> { setChipStyle(c, b); if (b) currentCategory = k; else if (k.equals(currentCategory)) currentCategory = null; }); chipGroupFilter.addView(c); }
    private void showRadiusSelectionDialog(Chip u) { BottomSheetDialog d = new BottomSheetDialog(this); View v = LayoutInflater.from(this).inflate(R.layout.layout_radius_bottom_sheet, null); ChipGroup g = v.findViewById(R.id.cgRadiusPresets); v.findViewById(R.id.btnApplyRadius).setOnClickListener(view -> { currentRadius = tempSelectedRadius; u.setText("Trong " + currentRadius + " km"); d.dismiss(); }); tempSelectedRadius = currentRadius; for (int km : RADIUS_OPTIONS) { Chip c = new Chip(this); c.setText(km + " km"); c.setCheckable(true); boolean s = (km == currentRadius); setChipStyle(c, s); if (s) c.setChecked(true); c.setOnCheckedChangeListener((view, isc) -> { if (isc) { tempSelectedRadius = km; setChipStyle(c, true); } else setChipStyle(c, false); }); g.addView(c); } d.setContentView(v); d.show(); }
    private void setChipStyle(Chip c, boolean s) { if (s) { c.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryBackground))); c.setTextColor(ContextCompat.getColor(this, R.color.white)); c.setChipStrokeWidth(0f); } else { c.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))); c.setTextColor(ContextCompat.getColor(this, R.color.colorGeneralText)); c.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_gray_stroke))); c.setChipStrokeWidth(1f); } }
    private void showFullFilterBottomSheet() { BottomSheetDialog d = new BottomSheetDialog(this); View v = LayoutInflater.from(this).inflate(R.layout.layout_filter_bottom_sheet, null); ChipGroup g = v.findViewById(R.id.chipGroupFullList); for (Map.Entry<String, String> e : CategoryUtils.CATEGORY_MAP.entrySet()) { Chip c = new Chip(this); c.setText(e.getValue()); c.setCheckable(true); boolean s = e.getKey().equals(currentCategory); if (s) c.setChecked(true); setChipStyle(c, s); c.setOnClickListener(view -> { currentCategory = e.getKey(); setupFilterChips(); d.dismiss(); }); g.addView(c); } d.setContentView(v); d.show(); }

    @Override public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) { super.onRequestPermissionsResult(r, p, g); if (r == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE && g.length > 0) checkAndGetLocation(); }
}