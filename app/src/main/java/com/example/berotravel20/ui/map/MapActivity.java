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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.data.repository.RouteRepository;
import com.example.berotravel20.utils.CategoryUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup; // IMPORT QUAN TRỌNG
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Helper & Repo
    private MapHelper mapHelper;
    private LocationHelper locationHelper;
    private PlaceRepository placeRepository;
    private RouteRepository routeRepository;

    // UI Components
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private EditText etSearch;
    private ProgressBar pbLoading;
    private ChipGroup chipGroupFilter;
    private RelativeLayout groupMainUI;

    // UI Search Result
    private LinearLayout layoutSearchResults;
    private TextView tvResultCount;
    private MapPlaceAdapter placeAdapter;

    // UI Preview Direction
    private LinearLayout layoutDirections;
    private DirectionStepAdapter stepAdapter;
    private TextView tvDuration, tvDistance;

    // [FIX] Dùng ToggleGroup, XÓA các biến ImageView lẻ tẻ
    private MaterialButtonToggleGroup toggleTransportMode;

    // UI Navigation Active
    private RelativeLayout layoutNavigationActive;
    private TextView navInstruction, navDistance;
    private ImageView navIcon;

    // State
    private Location currentUserLocation;
    private String currentKeyword = "";
    private String currentCategory = null;
    private int currentRadius = 5;
    private int tempSelectedRadius = 5;

    // Direction State
    private Place currentDestination;
    private String currentTransportProfile = "driving-car";
    private boolean isNavigating = false;
    private List<Step> currentRouteSteps;

    private final int[] RADIUS_OPTIONS = {1, 2, 5, 10, 15, 20, 30, 50};
    private final String[] POPULAR_FILTERS = {"restaurant", "cafe", "hotel", "atm", "gas_station", "hospital"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapHelper = new MapHelper(this);
        locationHelper = new LocationHelper(this);
        placeRepository = new PlaceRepository();
        routeRepository = new RouteRepository();

        initViews();
        setupFilterChips();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void initViews() {
        // --- 1. MAIN UI ---
        groupMainUI = findViewById(R.id.groupMainUI);

        etSearch = findViewById(R.id.etSearchMap);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch();
                return true;
            }
            return false;
        });
        findViewById(R.id.btnSearchIcon).setOnClickListener(v -> performSearch());

        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (currentUserLocation != null && mapHelper != null)
                mapHelper.moveCamera(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 15f);
            else checkAndGetLocation();
        });

        // --- 2. BOTTOM SHEET ---
        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(0.5f);
        bottomSheetBehavior.setPeekHeight(dpToPx(240));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // [QUAN TRỌNG] Cho phép ẩn bottom sheet khi vào Navigation Mode
        bottomSheetBehavior.setHideable(true);

        layoutSearchResults = findViewById(R.id.layout_search_results);
        layoutDirections = findViewById(R.id.layout_directions);
        pbLoading = findViewById(R.id.pbLoading);

        // --- 3. RESULT LIST ---
        tvResultCount = findViewById(R.id.tvResultCount);
        RecyclerView rvResults = findViewById(R.id.rvMapResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        placeAdapter = new MapPlaceAdapter(this, new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Place place) {
                mapHelper.moveCamera(new LatLng(place.latitude, place.longitude), 16f);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            @Override
            public void onDirectionClick(Place place) { enterPreviewDirectionMode(place); }
        });
        rvResults.setAdapter(placeAdapter);

        // --- 4. PREVIEW DIRECTION UI ---
        tvDuration = findViewById(R.id.tvDuration);
        tvDistance = findViewById(R.id.tvDistance);

        // [FIX] Ánh xạ ToggleGroup thay vì tìm từng nút con
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

        // --- 5. ACTIVE NAVIGATION UI ---
        layoutNavigationActive = findViewById(R.id.layoutNavigationActive);
        navInstruction = findViewById(R.id.navStepInstruction);
        navDistance = findViewById(R.id.navStepDistance);
        navIcon = findViewById(R.id.navStepIcon);

        findViewById(R.id.btnStopNavigation).setOnClickListener(v -> stopNavigation());
    }

    // --- LOGIC LOGIC ---
    private void selectTransportMode(String mode) {
        if (mode.equals(currentTransportProfile)) return;
        currentTransportProfile = mode;
        // Không cần update UI thủ công vì ToggleGroup tự lo
        if (currentDestination != null) fetchRoute(currentDestination);
    }

    private void enterPreviewDirectionMode(Place place) {
        this.currentDestination = place;
        layoutSearchResults.setVisibility(View.GONE);
        layoutDirections.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

        // Reset về Car
        currentTransportProfile = "driving-car";
        toggleTransportMode.check(R.id.btnModeCar);

        fetchRoute(place);
    }

    private void exitPreviewDirectionMode() {
        mapHelper.clearPolyline();
        layoutDirections.setVisibility(View.GONE);
        layoutSearchResults.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        this.currentDestination = null;
        if(currentUserLocation!=null) mapHelper.moveCamera(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 15f);
    }

    private void startNavigation() {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();
            return;
        }
        isNavigating = true;

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        groupMainUI.setVisibility(View.GONE);
        layoutNavigationActive.setVisibility(View.VISIBLE);

        mapHelper.disableMyLocationLayer();
        mapHelper.updateNavigationCamera(currentUserLocation);

        locationHelper.startLocationUpdates(location -> {
            currentUserLocation = location;
            if (isNavigating) {
                mapHelper.updateNavigationCamera(location);
                // Demo Update Step
                if (currentRouteSteps != null && !currentRouteSteps.isEmpty()) {
                    Step s = currentRouteSteps.get(0);
                    navInstruction.setText(s.instruction);
                    navDistance.setText((int)s.distance + " m");
                }
            }
        });

        if (currentRouteSteps != null && !currentRouteSteps.isEmpty()) {
            Step s = currentRouteSteps.get(0);
            navInstruction.setText(s.instruction);
            navDistance.setText((int)s.distance + " m");
        }
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

    // ... (Giữ nguyên các phần fetchRoute, onMapReady, checkAndGetLocation, search, filter...)

    // Copy lại phần fetchRoute, performSearch, v.v... từ code cũ nếu cần, hoặc giữ nguyên
    // Vì phần này không gây lỗi ClassCastException.

    // --- API ROUTE ---
    private void fetchRoute(Place destination) {
        if (currentUserLocation == null) {
            checkAndGetLocation();
            return;
        }
        tvDuration.setText("...");
        tvDistance.setText("");

        routeRepository.getRoute(currentTransportProfile, currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), destination.latitude, destination.longitude,
                new RouteRepository.RouteCallback() {
                    @Override
                    public void onSuccess(List<LatLng> path, List<Step> steps, double dist, double dur) {
                        mapHelper.drawPolyline(path);
                        currentRouteSteps = steps;

                        if (dist < 1000) tvDistance.setText(String.format("(%d m)", (int)dist));
                        else tvDistance.setText(String.format("(%.1f km)", dist/1000));

                        int min = (int)(dur/60);
                        tvDuration.setText(min + " phút");
                        if(stepAdapter != null) stepAdapter.setData(steps);
                    }
                    @Override public void onError(String msg) { tvDuration.setText("Lỗi"); }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapHelper.setGoogleMap(googleMap);
        googleMap.setOnMapClickListener(l -> {
            if(!isNavigating) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideKeyboard();
        });

        checkAndGetLocation();
        loadAllPlaces();
    }

    private void checkAndGetLocation() {
        locationHelper.getLastLocation(location -> {
            currentUserLocation = location;
            // Chỉ move camera nếu chưa chọn điểm đến (tránh giật khi đang xem đường)
            if (currentDestination == null) {
                mapHelper.moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), 15f);
            }
            if(locationHelper.hasPermission()) mapHelper.enableMyLocationLayer();
        });
    }

    private void loadAllPlaces() {
        showLoading();
        mapHelper.clearPolyline();
        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override public void onSuccess(List<Place> data) { hideLoading(); updateSearchResults(data); }
            @Override public void onError(String message) { hideLoading(); }
        });
    }

    private void performSearch() {
        if (currentUserLocation == null) { checkAndGetLocation(); return; }
        if (etSearch != null) currentKeyword = etSearch.getText().toString().trim();
        if (currentDestination == null) mapHelper.drawSearchRadius(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), currentRadius);

        showLoading();
        placeRepository.searchNearby(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(), currentRadius, currentKeyword.isEmpty()?"":currentKeyword, currentCategory,
                new DataCallback<List<Place>>() {
                    @Override public void onSuccess(List<Place> data) {
                        hideLoading(); updateSearchResults(data);
                        if(data!=null && !data.isEmpty() && currentDestination==null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    @Override public void onError(String message) { hideLoading(); }
                });
    }

    private void updateSearchResults(List<Place> places) {
        runOnUiThread(() -> {
            if(placeAdapter!=null) placeAdapter.setData(places);
            if(tvResultCount!=null) tvResultCount.setText("Kết quả (" + (places!=null?places.size():0) + ")");
            mapHelper.showMarkers(places);
        });
    }

    private void setupFilterChips() {
        chipGroupFilter.removeAllViews();
        addRadiusChip();
        for (String key : POPULAR_FILTERS) addCategoryChip(key, CategoryUtils.getLabel(key));
        Chip more = new Chip(this); more.setText("Thêm..."); setChipStyle(more, false);
        more.setOnClickListener(v -> showFullFilterBottomSheet());
        chipGroupFilter.addView(more);
    }

    private void addRadiusChip() {
        Chip c = new Chip(this); c.setText("Trong "+currentRadius+" km"); c.setChipIconResource(R.drawable.ic_search_gray);
        setChipStyle(c, false); c.setOnClickListener(v->showRadiusSelectionDialog(c));
        chipGroupFilter.addView(c);
    }

    private void addCategoryChip(String key, String label) {
        Chip c = new Chip(this); c.setText(label); c.setCheckable(true);
        setChipStyle(c, key.equals(currentCategory));
        c.setOnCheckedChangeListener((v,b)->{ setChipStyle(c,b); if(b) currentCategory=key; else if(key.equals(currentCategory)) currentCategory=null; });
        chipGroupFilter.addView(c);
    }

    private void showRadiusSelectionDialog(Chip updateChip) {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.layout_radius_bottom_sheet, null);
        ChipGroup g = v.findViewById(R.id.cgRadiusPresets);
        Button btn = v.findViewById(R.id.btnApplyRadius);
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryBackground)));
        btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        tempSelectedRadius = currentRadius;
        for(int km : RADIUS_OPTIONS) {
            Chip c = new Chip(this); c.setText(km+" km"); c.setCheckable(true); c.setClickable(true);
            boolean sel = (km==currentRadius); setChipStyle(c, sel); if(sel) c.setChecked(true);
            c.setOnCheckedChangeListener((view,b)->{ if(b) { tempSelectedRadius=km; setChipStyle(c,true); } else setChipStyle(c,false); });
            g.addView(c);
        }
        btn.setOnClickListener(view -> { currentRadius=tempSelectedRadius; updateChip.setText("Trong "+currentRadius+" km"); d.dismiss(); });
        d.setContentView(v); d.show();
    }

    private void setChipStyle(Chip c, boolean sel) {
        if(sel) {
            c.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryBackground)));
            c.setTextColor(ContextCompat.getColor(this, R.color.white)); c.setChipStrokeWidth(0f);
            if(c.getChipIcon()!=null) c.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        } else {
            c.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            c.setTextColor(ContextCompat.getColor(this, R.color.colorGeneralText)); c.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_gray_stroke))); c.setChipStrokeWidth(1f);
            if(c.getChipIcon()!=null) c.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGeneralText)));
        }
    }

    private void showFullFilterBottomSheet() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.layout_filter_bottom_sheet, null);
        ChipGroup g = v.findViewById(R.id.chipGroupFullList);
        for(java.util.Map.Entry<String,String> e : CategoryUtils.CATEGORY_MAP.entrySet()) {
            Chip c = new Chip(this); c.setText(e.getValue()); c.setCheckable(true);
            boolean sel = e.getKey().equals(currentCategory); if(sel) c.setChecked(true); setChipStyle(c, sel);
            c.setOnClickListener(view->{ currentCategory=e.getKey(); setupFilterChips(); d.dismiss(); });
            g.addView(c);
        }
        d.setContentView(v); d.show();
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }
    private void showLoading() { if(pbLoading!=null) pbLoading.setVisibility(View.VISIBLE); }
    private void hideLoading() { if(pbLoading!=null) pbLoading.setVisibility(View.GONE); }
    private void hideKeyboard() {
        View v = getCurrentFocus();
        if(v!=null) ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) {
        super.onRequestPermissionsResult(r, p, g);
        if(r==LocationHelper.LOCATION_PERMISSION_REQUEST_CODE && g.length>0) checkAndGetLocation();
    }
}