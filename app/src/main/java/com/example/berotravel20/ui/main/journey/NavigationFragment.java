package com.example.berotravel20.ui.main.journey;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.berotravel20.R;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.data.model.ORS.Step;
import com.example.berotravel20.data.repository.JourneyRepository;
import com.example.berotravel20.data.repository.RouteRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.map.LocationHelper;
import com.example.berotravel20.ui.map.MapHelper;
import com.example.berotravel20.ui.map.MapNavigationManager;
import com.example.berotravel20.utils.ToastUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class NavigationFragment extends BaseFragment implements
        OnMapReadyCallback,
        MapNavigationManager.NavigationListener {

    // --- UI Components ---
    private GoogleMap mMap;
    private TextView tvNavName, tvNavAddress, tvNavInstruction, tvNavDistanceStep;
    private ImageView imgNavIcon;
    private CardView layoutNavInstruction;
    private View btnRecenter;

    // --- Dialog Components (MỚI) ---
    private Dialog customLoadingDialog;
    private Dialog customAlertDialog;

    // --- Repositories & Helpers ---
    private JourneyRepository journeyRepository;
    private RouteRepository routeRepository;
    private MapHelper mapHelper;
    private LocationHelper locationHelper;
    private MapNavigationManager navigationManager;

    // --- Data & State ---
    private Journey journey;
    private int currentTargetIdx = -1;
    private Location currentUserLocation;
    private boolean isFetchingRoute = false;

    public static NavigationFragment newInstance(Journey j) {
        NavigationFragment f = new NavigationFragment();
        Bundle b = new Bundle();
        b.putSerializable("J_DATA", j);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            journey = (Journey) getArguments().getSerializable("J_DATA");
        }

        journeyRepository = new JourneyRepository(requireContext());
        routeRepository = new RouteRepository();
        mapHelper = new MapHelper(requireContext());
        locationHelper = new LocationHelper(requireActivity());
        navigationManager = new MapNavigationManager(this);

        tvNavName = view.findViewById(R.id.tv_nav_name);
        tvNavAddress = view.findViewById(R.id.tv_nav_address);
        tvNavInstruction = view.findViewById(R.id.tv_nav_instruction);
        tvNavDistanceStep = view.findViewById(R.id.tv_nav_distance_step);
        imgNavIcon = view.findViewById(R.id.img_nav_icon);
        layoutNavInstruction = view.findViewById(R.id.layout_nav_instruction);
        btnRecenter = view.findViewById(R.id.btn_recenter);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        view.findViewById(R.id.btn_complete).setOnClickListener(v -> handleCheckIn());
        view.findViewById(R.id.btn_suspend).setOnClickListener(v -> handleSuspend());
        btnRecenter.setOnClickListener(v -> {
            if (currentUserLocation != null) mapHelper.updateNavigationCamera(currentUserLocation);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mapHelper.setGoogleMap(googleMap);

        if (locationHelper.hasPermission()) {
            mapHelper.enableMyLocationLayer();
        }

        locationHelper.getLastLocation(location -> {
            currentUserLocation = location;
            findNextPlaceToNavigate();
            startLocationTracking();
        });
    }

    private void findNextPlaceToNavigate() {
        if (journey == null || journey.places == null) return;

        currentTargetIdx = -1;
        for (int i = 0; i < journey.places.size(); i++) {
            if (!journey.places.get(i).visited && journey.places.get(i).place != null) {
                currentTargetIdx = i;
                break;
            }
        }

        if (currentTargetIdx != -1) {
            fetchRoute(journey.places.get(currentTargetIdx).place);
        } else {
            showFinalCompletionDialog();
        }
    }

    private void fetchRoute(Journey.PlaceDetail destination) {
        if (currentUserLocation == null || isFetchingRoute) return;
        isFetchingRoute = true;

        tvNavName.setText("Đến: " + destination.name);
        tvNavAddress.setText(destination.address);
        tvNavInstruction.setText("Đang tìm đường...");

        routeRepository.getRoute("driving-car",
                currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                destination.latitude, destination.longitude,
                new RouteRepository.RouteCallback() {
                    @Override
                    public void onSuccess(List<LatLng> polyline, List<Step> steps, double distance, double duration) {
                        isFetchingRoute = false;
                        mapHelper.clearMarkers();
                        mapHelper.drawPolyline(polyline);

                        // Tạo object Place tạm để khớp với MapHelper
                        com.example.berotravel20.data.model.Place.Place tempPlace = new com.example.berotravel20.data.model.Place.Place();
                        tempPlace.latitude = destination.latitude;
                        tempPlace.longitude = destination.longitude;
                        tempPlace.name = destination.name;
                        tempPlace.address = destination.address;
                        mapHelper.addDestinationMarker(tempPlace);

                        navigationManager.startNewRoute(steps, polyline);
                        mapHelper.updateNavigationCamera(currentUserLocation);
                    }

                    @Override
                    public void onError(String message) {
                        isFetchingRoute = false;
                        tvNavInstruction.setText("Lỗi tìm đường");
                        showCustomAlert("Lỗi Route", message, true); // Dùng Dialog thay Toast
                    }
                });
    }

    private void startLocationTracking() {
        locationHelper.startLocationUpdates(location -> {
            currentUserLocation = location;
            mapHelper.updateNavigationCamera(location);
            navigationManager.onLocationUpdated(location);
        });
    }

    // --- Navigation Callbacks ---
    @Override
    public void onUpdateInstruction(String instruction, String distance) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvNavInstruction.setText(instruction);
                tvNavDistanceStep.setText(distance);
            });
        }
    }

    @Override
    public void onNextStep(String instruction) {
        ToastUtils.show(requireContext(), "Tiếp theo: " + instruction, ToastUtils.INFO);
    }

    @Override
    public void onArrived() {
        showCustomAlert("Thông báo", "Bạn đã đến nơi!", false);
    }

    @Override
    public void onRerouteNeeded() {
        if (currentTargetIdx != -1) {
            fetchRoute(journey.places.get(currentTargetIdx).place);
        }
    }

    // --- Actions ---

    private void handleCheckIn() {
        if (currentTargetIdx == -1 || journey == null || journey.places == null) {
            showCustomAlert("Lỗi", "Dữ liệu hành trình không hợp lệ", true);
            return;
        }

        Journey.JourneyPlace currentItem = journey.places.get(currentTargetIdx);
        if (currentItem.place == null) {
            showCustomAlert("Lỗi", "Địa điểm này không có thông tin chi tiết", true);
            return;
        }

        String realPlaceId = currentItem.place.id;
        String journeyId = journey.id;

        showCustomLoading(); // HIỆN DIALOG LOADING MỚI

        journeyRepository.markPlaceVisited(journeyId, realPlaceId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hideCustomLoading(); // ẨN DIALOG LOADING
                showCustomAlert("Tuyệt vời!", "Check-in thành công!", false);

                journey.places.get(currentTargetIdx).visited = true;
                mapHelper.clearPolyline();
                findNextPlaceToNavigate();
            }

            @Override
            public void onError(String message) {
                hideCustomLoading(); // ẨN DIALOG LOADING
                showCustomAlert("Lỗi Check-in", message, true); // HIỆN DIALOG LỖI
            }
        });
    }

    private void handleSuspend() {
        showCustomLoading();
        journeyRepository.updateStatus(journey.id, "suspended", new DataCallback<Void>() {
            @Override public void onSuccess(Void d) {
                hideCustomLoading();
                getParentFragmentManager().popBackStack();
            }
            @Override public void onError(String m) {
                hideCustomLoading();
                showCustomAlert("Lỗi", "Không thể tạm dừng: " + m, true);
            }
        });
    }

    private void showFinalCompletionDialog() {
        locationHelper.stopLocationUpdates();
        showCustomAlert("Hoàn thành!", "Chúc mừng bạn đã hoàn thành tất cả các điểm đến!", false);
        // Lưu ý: Logic nút OK của Dialog này cần sửa chút nếu muốn nó back về
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationHelper != null) locationHelper.stopLocationUpdates();
        // Dismiss để tránh lỗi WindowLeaked
        if (customLoadingDialog != null && customLoadingDialog.isShowing()) customLoadingDialog.dismiss();
        if (customAlertDialog != null && customAlertDialog.isShowing()) customAlertDialog.dismiss();
    }

    // =================================================================================
    // ====================== PHẦN CUSTOM DIALOG RIÊNG CHO NAV =========================
    // =================================================================================

    private void showCustomLoading() {
        if (customLoadingDialog == null) {
            customLoadingDialog = new Dialog(requireContext());
            customLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customLoadingDialog.setContentView(R.layout.dialog_loading); // Layout đã tạo
            if (customLoadingDialog.getWindow() != null) {
                customLoadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                customLoadingDialog.getWindow().setDimAmount(0.5f);
            }
            customLoadingDialog.setCancelable(false);
        }
        if (!customLoadingDialog.isShowing()) {
            customLoadingDialog.show();
        }
    }

    private void hideCustomLoading() {
        if (customLoadingDialog != null && customLoadingDialog.isShowing()) {
            customLoadingDialog.dismiss();
        }
    }

    private void showCustomAlert(String title, String message, boolean isError) {
        if (getContext() == null) return;

        customAlertDialog = new Dialog(requireContext());
        customAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customAlertDialog.setContentView(R.layout.dialog_alert); // Layout đã tạo

        if (customAlertDialog.getWindow() != null) {
            customAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = customAlertDialog.findViewById(R.id.tv_dialog_title);
        TextView tvMessage = customAlertDialog.findViewById(R.id.tv_dialog_message);
        ImageView imgIcon = customAlertDialog.findViewById(R.id.img_dialog_icon);
        Button btnOk = customAlertDialog.findViewById(R.id.btn_dialog_ok);

        tvTitle.setText(title);
        tvMessage.setText(message);

        if (isError) {
            imgIcon.setImageResource(R.drawable.ic_warning);
            imgIcon.setColorFilter(Color.parseColor("#F44336"));
            tvTitle.setTextColor(Color.parseColor("#F44336"));
        } else {
            imgIcon.setImageResource(R.drawable.ic_check_circle); // Nhớ tạo icon này
            imgIcon.setColorFilter(Color.parseColor("#009688"));
            tvTitle.setTextColor(Color.parseColor("#009688"));
        }

        btnOk.setOnClickListener(v -> {
            customAlertDialog.dismiss();
            // Nếu là thông báo hoàn thành toàn bộ, bấm OK sẽ thoát
            if (title.equals("Hoàn thành!")) {
                getParentFragmentManager().popBackStack();
            }
        });

        customAlertDialog.show();
    }
}