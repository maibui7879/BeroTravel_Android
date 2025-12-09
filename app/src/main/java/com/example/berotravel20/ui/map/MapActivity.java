package com.example.berotravel20.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.repository.PlaceRepository;
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
    private ProgressBar pbLoading; // Biến Loading

    private PlaceRepository placeRepository;
    private Map<String, Place> markerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate: MapActivity started");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        placeRepository = new PlaceRepository();
        initViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initViews() {
        // 1. Search Bar (Keyboard)
        etSearch = findViewById(R.id.etSearchMap);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String nameInput = etSearch.getText().toString().trim();
                if (!nameInput.isEmpty()) {
                    performSearch(nameInput);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        // 2. Search Icon Click
        ImageView btnSearchIcon = findViewById(R.id.btnSearchIcon);
        btnSearchIcon.setOnClickListener(v -> {
            String nameInput = etSearch.getText().toString().trim();
            if (!nameInput.isEmpty()) {
                performSearch(nameInput);
                hideKeyboard();
            } else {
                Toast.makeText(this, "Vui lòng nhập tên địa điểm", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Custom My Location Button Click
        View btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(v -> {
            Log.d(TAG, "Click My Location Button");
            enableMyLocation(); // Gọi lại hàm lấy vị trí
        });

        // Setup BottomSheet
        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        tvResultCount = findViewById(R.id.tvResultCount);
        pbLoading = findViewById(R.id.pbLoading); // Ánh xạ ProgressBar

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

    // Helper: Ẩn bàn phím
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Helper: Hiện Loading
    private void showLoading() {
        runOnUiThread(() -> {
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
            if (rvResults != null) rvResults.setVisibility(View.GONE);
            if (tvResultCount != null) tvResultCount.setText("Đang tìm kiếm...");
        });
    }

    // Helper: Tắt Loading
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
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // Tắt nút mặc định của Google

        enableMyLocation(); // Bật layer vị trí

        mMap.setOnMarkerClickListener(marker -> {
            Place place = markerMap.get(marker.getId());
            if (place != null) marker.showInfoWindow();
            return false;
        });

        mMap.setOnMapClickListener(latLng -> {
            if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideKeyboard();
        });

        loadAllPlaces();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateUserLocation(location);
            } else {
                requestCurrentLocation();
            }
        });
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CancellationTokenSource tokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateUserLocation(location);
                    } else {
                        // Fallback Hà Nội
                        LatLng hanoi = new LatLng(21.0285, 105.8542);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 15f));
                    }
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
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    // --- LOGIC API ---

    private void loadAllPlaces() {
        Log.d(TAG, ">>> API CALL: loadAllPlaces");
        showLoading(); // Bật loading

        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading(); // Tắt loading
                updateMapAndList(data);
            }

            @Override
            public void onError(String message) {
                hideLoading(); // Tắt loading
                Log.e(TAG, "<<< API ERROR: " + message);
            }
        });
    }

    private void performSearch(String name) {
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

        Log.d(TAG, ">>> API CALL: searchNearby | Name: '" + name + "'");
        showLoading(); // Bật loading

        placeRepository.searchNearby(lat, lng, 10, name, null, new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                hideLoading(); // Tắt loading

                updateMapAndList(data);

                if (data != null && !data.isEmpty()) {
                    // Không mở full nữa, giữ nguyên trạng thái Collapsed
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    Toast.makeText(MapActivity.this, "Không tìm thấy kết quả nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                hideLoading(); // Tắt loading
                Log.e(TAG, "<<< API ERROR: " + message);
                Toast.makeText(MapActivity.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapAndList(List<Place> places) {
        runOnUiThread(() -> {
            if (places == null) return;

            if (adapter != null) {
                adapter.setData(places);
            }

            if (tvResultCount != null) {
                tvResultCount.setText("Kết quả tìm kiếm (" + places.size() + ")");
            }

            if (rvResults != null) {
                rvResults.scrollToPosition(0);
            }

            if (mMap != null) {
                mMap.clear();
                markerMap.clear();

                for (Place place : places) {
                    LatLng location = new LatLng(place.latitude, place.longitude);
                    MarkerOptions options = new MarkerOptions()
                            .position(location)
                            .title(place.name)
                            .snippet(place.address);
                    Marker marker = mMap.addMarker(options);
                    if (marker != null) markerMap.put(marker.getId(), place);
                }
            }
        });
    }
}