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
import com.example.berotravel20.ui.common.BaseActivity; // Import BaseActivity
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

        fetchMyFavorites();
    }

    private void initViews() {
        groupMainUI = findViewById(R.id.groupMainUI);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearchMap);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch();
                MapUtils.hideKeyboard(this);
                return true;
            }
            return false;
        });

        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (currentUserLocation != null)
                mapHelper.moveCamera(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 15f);
            else checkAndGetLocation();
        });

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(MapUtils.dpToPx(this, 240));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        layoutSearchResults = findViewById(R.id.layout_search_results);
        pbLoading = findViewById(R.id.pbLoading);
        tvResultCount = findViewById(R.id.tvResultCount);
        rvMapResults = findViewById(R.id.rvMapResults);
        rvMapResults.setLayoutManager(new LinearLayoutManager(this));

        // --- CẬP NHẬT LOGIC CLICK: QUAY VỀ BASE_ACTIVITY ĐỂ MỞ FRAGMENT ---
        placeAdapter = new MapPlaceAdapter(this, new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onFavoriteClick(Place place) {
                handleFavoriteToggle(place);
            }

            @Override
            public void onItemClick(Place place) {
                // Tạo Intent quay về BaseActivity
                Intent intent = new Intent(MapActivity.this, BaseActivity.class);

                // Gửi dữ liệu theo đúng cấu trúc BaseActivity yêu cầu
                intent.putExtra("NAVIGATE_TO", "PLACE_DETAIL");
                intent.putExtra("PLACE_ID", place.id);

                // Flags giúp quay về Activity hiện tại (nếu đang chạy ngầm) mà không tạo mới
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intent);
                finish(); // Đóng MapActivity
            }

            @Override
            public void onDirectionClick(Place place) {
                enterPreviewDirectionMode(place);
            }
        });
        rvMapResults.setAdapter(placeAdapter);

        btnLoadMore = findViewById(R.id.btnLoadMore);
        btnLoadMore.setOnClickListener(v -> loadMorePlaces());
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

    @Override
    public void onMapReady(@NonNull GoogleMap g) {
        mapHelper.setGoogleMap(g);
        handleIntentData();
        if (currentKeyword.isEmpty() && currentDestination == null) {
            loadAllPlaces();
        }
        checkAndGetLocation();
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        if ("DIRECT_TO_PLACE".equals(intent.getStringExtra("ACTION_TYPE"))) {
            double lat = intent.getDoubleExtra("TARGET_LAT", 0);
            double lng = intent.getDoubleExtra("TARGET_LNG", 0);
            String name = intent.getStringExtra("TARGET_NAME");
            if (lat != 0) {
                currentDestination = new Place();
                currentDestination.latitude = lat; currentDestination.longitude = lng; currentDestination.name = name;
            }
        }

        if (intent.hasExtra("SEARCH_QUERY")) {
            currentKeyword = intent.getStringExtra("SEARCH_QUERY");
            if (etSearch != null) etSearch.setText(currentKeyword);
        }
    }

    private void checkAndGetLocation() {
        locationHelper.getLastLocation(l -> {
            currentUserLocation = l;
            if (l == null) return;
            if (currentDestination == null) {
                mapHelper.moveCamera(new LatLng(l.getLatitude(), l.getLongitude()), 15f);
            }
            if (locationHelper.hasPermission()) mapHelper.enableMyLocationLayer();

            if (!currentKeyword.isEmpty()) {
                resetAndCallSearchApi(null, null);
            } else if (currentDestination != null) {
                enterPreviewDirectionMode(currentDestination);
            }
        });
    }

    private void performSearch() {
        currentKeyword = etSearch.getText().toString().trim();
        if (currentKeyword.length() < 2) return;
        resetAndCallSearchApi(null, null);
    }

    private void resetAndCallSearchApi(Integer page, Integer limit) {
        currentPage = 1; isLoadingMore = true;
        placeAdapter.clearData();
        if (currentUserLocation != null)
            mapHelper.drawSearchRadius(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), currentRadius);
        callSearchNearbyApi(page, limit);
    }

    private void callSearchNearbyApi(Integer pageToLoad, Integer limit) {
        if (currentUserLocation == null) return;
        showLoading();
        placeRepository.searchNearby(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), currentRadius, currentKeyword, currentCategory, pageToLoad, limit,
                new DataCallback<PlaceResponse>() {
                    @Override
                    public void onSuccess(PlaceResponse response) {
                        hideLoading();
                        isLoadingMore = false;
                        if (response != null && response.data != null) handleSearchResponse(response, pageToLoad);
                    }
                    @Override public void onError(String message) { hideLoading(); }
                });
    }

    private void handleSearchResponse(PlaceResponse response, Integer pageToLoad) {
        boolean isFirst = (pageToLoad == null || pageToLoad == 1);
        if (isFirst) {
            totalPages = (response.data.size() >= 10) ? 2 : 1;
            tvResultCount.setText("Kết quả (" + response.data.size() + ")");
            updateMapAndList(response.data, true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            totalPages = response.totalPages;
            updateMapAndList(response.data, false);
        }
        btnLoadMore.setVisibility(currentPage < totalPages ? View.VISIBLE : View.GONE);
    }

    private void loadAllPlaces() {
        showLoading();
        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading();
                if (data != null) {
                    updateMapAndList(data, true);
                    tvResultCount.setText("Khám phá địa điểm");
                }
            }
            @Override public void onError(String m) { hideLoading(); }
        });
    }

    private void updateMapAndList(List<Place> places, boolean isReset) {
        runOnUiThread(() -> {
            if (isReset) {
                placeAdapter.setData(places);
                mapHelper.showMarkers(places);
            } else {
                placeAdapter.addData(places);
            }
        });
    }

    private void loadMorePlaces() {
        if (isLoadingMore) return;
        currentPage++;
        callSearchNearbyApi(currentPage, 10);
    }

    private void enterPreviewDirectionMode(Place p) {
        currentDestination = p;
        layoutSearchResults.setVisibility(View.GONE);
        layoutDirections.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        mapHelper.clearMarkers();
        mapHelper.addDestinationMarker(p);
        fetchRoute(p);
    }

    private void exitPreviewDirectionMode() {
        mapHelper.clearPolyline();
        layoutDirections.setVisibility(View.GONE);
        layoutSearchResults.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        currentDestination = null;
        loadAllPlaces();
    }

    private void fetchRoute(Place d) {
        if (currentUserLocation == null || isFetchingRoute) return;
        isFetchingRoute = true;
        routeRepository.getRoute(currentTransportProfile, currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), d.latitude, d.longitude, new RouteRepository.RouteCallback() {
            @Override
            public void onSuccess(List<LatLng> p, List<Step> s, double dis, double dur) {
                isFetchingRoute = false;
                mapHelper.drawPolyline(p);
                navigationManager.startNewRoute(s, p);
                tvDistance.setText(dis < 1000 ? (int) dis + " m" : String.format("%.1f km", dis / 1000));
                tvDuration.setText(formatDuration(dur));
                stepAdapter.setData(s);
            }
            @Override public void onError(String m) { isFetchingRoute = false; }
        });
    }

    private void startNavigation() {
        isNavigating = true;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        groupMainUI.setVisibility(View.GONE);
        layoutNavigationActive.setVisibility(View.VISIBLE);
        mapHelper.disableMyLocationLayer();
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

    private void selectTransportMode(String m) {
        currentTransportProfile = m;
        if (currentDestination != null) fetchRoute(currentDestination);
    }

    private String formatDuration(double durationSeconds) {
        long totalMinutes = (long) (durationSeconds / 60);
        if (totalMinutes < 60) return totalMinutes + " phút";
        else return (totalMinutes / 60) + " giờ " + (totalMinutes % 60) + " phút";
    }

    private void fetchMyFavorites() {
        if (TokenManager.getInstance(this).getToken() == null) return;
        favoriteRepository.getMyFavorites(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> places) {
                List<String> ids = new ArrayList<>();
                if (places != null) for (Place p : places) ids.add(p.id);
                placeAdapter.setFavoriteIds(ids);
            }
            @Override public void onError(String message) {}
        });
    }

    private void handleFavoriteToggle(Place place) {
        if (TokenManager.getInstance(this).getToken() == null) {
            RequestLoginDialog.newInstance().show(getSupportFragmentManager(), "LoginReq");
            return;
        }
        favoriteRepository.toggleFavorite(place.id, new DataCallback<FavoriteResponse>() {
            @Override
            public void onSuccess(FavoriteResponse data) {
                ToastUtils.showSuccess(MapActivity.this, data.message);
                placeAdapter.toggleFavoriteLocal(place.id);
            }
            @Override public void onError(String message) { ToastUtils.showError(MapActivity.this, message); }
        });
    }

    private void setupFilterChips() {
        chipGroupFilter.removeAllViews();
        addRadiusChip();
        for (String k : POPULAR_FILTERS) addCategoryChip(k, CategoryUtils.getLabel(k));
    }

    private void addRadiusChip() {
        Chip c = new Chip(this); c.setText("Bán kính: " + currentRadius + " km");
        c.setOnClickListener(v -> showRadiusSelectionDialog(c));
        chipGroupFilter.addView(c);
    }

    private void addCategoryChip(String k, String l) {
        Chip c = new Chip(this); c.setText(l); c.setCheckable(true);
        c.setOnCheckedChangeListener((v, b) -> { currentCategory = b ? k : null; performSearch(); });
        chipGroupFilter.addView(c);
    }

    private void showRadiusSelectionDialog(Chip u) {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.layout_radius_bottom_sheet, null);
        ChipGroup g = v.findViewById(R.id.cgRadiusPresets);
        v.findViewById(R.id.btnApplyRadius).setOnClickListener(view -> { currentRadius = tempSelectedRadius; u.setText("Bán kính: " + currentRadius + " km"); performSearch(); d.dismiss(); });
        for (int km : RADIUS_OPTIONS) {
            Chip c = new Chip(this); c.setText(km + " km"); c.setCheckable(true);
            if (km == currentRadius) c.setChecked(true);
            c.setOnCheckedChangeListener((view, isc) -> { if (isc) tempSelectedRadius = km; });
            g.addView(c);
        }
        d.setContentView(v); d.show();
    }

    private void showLoading() { if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE); }
    private void hideLoading() { if (pbLoading != null) pbLoading.setVisibility(View.GONE); }

    private boolean isUserLoggedIn() { return TokenManager.getInstance(this).getToken() != null; }
    @Override public void onLoginClick() { startActivity(new Intent(this, AuthActivity.class)); }
    @Override public void onCancelClick() {}

    @Override public void onUpdateInstruction(String i, String d) { runOnUiThread(() -> { navInstruction.setText(i); navDistance.setText(d); }); }
    @Override public void onNextStep(String i) {}
    @Override public void onArrived() { runOnUiThread(() -> { ToastUtils.showSuccess(this, "Đến nơi"); stopNavigation(); }); }
    @Override public void onRerouteNeeded() { if (!isFetchingRoute) fetchRoute(currentDestination); }
    @Override public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) { if (r == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) checkAndGetLocation(); }
}