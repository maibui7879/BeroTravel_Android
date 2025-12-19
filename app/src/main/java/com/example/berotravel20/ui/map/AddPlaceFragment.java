package com.example.berotravel20.ui.map;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.berotravel20.R;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.utils.ImageUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPlaceFragment extends BaseFragment implements OnMapReadyCallback {
    private double lat, lng;
    private String mEditPlaceId = null;
    private boolean isEditMode = false;
    private PlaceRepository repository;
    private String base64MainImage = null;

    private EditText etName, etAddress, etDesc, etLat, etLng;
    private AutoCompleteTextView spinnerCategory;
    private ImageView ivMainPreview;
    private TextView tvPlaceholder, tvFormTitle;
    private GoogleMap mMap;
    private Marker currentMarker;

    private final Map<String, String> categoryMap = new HashMap<String, String>() {{
        put("Khách sạn", "hotel"); put("Nhà hàng", "restaurant");
        put("Điểm tham quan", "tourist_attraction"); put("Công viên", "park");
        put("Quán Bar", "bar"); put("Cà phê", "cafe");
    }};

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    ivMainPreview.setImageURI(uri);
                    tvPlaceholder.setVisibility(View.GONE);
                    base64MainImage = ImageUtils.uriToBase64(requireContext(), uri);
                }
            });

    public static AddPlaceFragment newInstance(double lat, double lng) {
        AddPlaceFragment fragment = new AddPlaceFragment();
        Bundle args = new Bundle();
        args.putDouble("LAT", lat); args.putDouble("LNG", lng);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddPlaceFragment newInstanceForEdit(String placeId, double lat, double lng) {
        AddPlaceFragment fragment = new AddPlaceFragment();
        Bundle args = new Bundle();
        args.putString("EDIT_PLACE_ID", placeId);
        args.putDouble("LAT", lat); args.putDouble("LNG", lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lat = getArguments().getDouble("LAT");
            lng = getArguments().getDouble("LNG");
            mEditPlaceId = getArguments().getString("EDIT_PLACE_ID");
            isEditMode = (mEditPlaceId != null);
        }
        repository = new PlaceRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupCategorySpinner();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_picker);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        if (isEditMode) {
            tvFormTitle.setText("Chỉnh sửa địa điểm");
            loadExistingPlaceData();
        }

        view.findViewById(R.id.btn_select_main_image).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        view.findViewById(R.id.btn_save_place).setOnClickListener(v -> savePlace());

        // Handle Exit thông minh để không thoát App
        View.OnClickListener closeAction = v -> handleExit();
        view.findViewById(R.id.btn_cancel).setOnClickListener(closeAction);
        view.findViewById(R.id.btn_close).setOnClickListener(closeAction);
    }

    private void loadExistingPlaceData() {
        showLoading();
        repository.getPlaceById(mEditPlaceId, new DataCallback<Place>() {
            @Override
            public void onSuccess(Place place) {
                hideLoading();
                etName.setText(place.name);
                etAddress.setText(place.address);
                etDesc.setText(place.description);
                for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                    if (entry.getValue().equals(place.category)) {
                        spinnerCategory.setText(entry.getKey(), false); break;
                    }
                }
                if (place.imageUrl != null) {
                    tvPlaceholder.setVisibility(View.GONE);
                    com.bumptech.glide.Glide.with(AddPlaceFragment.this).load(place.imageUrl).into(ivMainPreview);
                }
            }
            @Override public void onError(String msg) { hideLoading(); showError(msg); }
        });
    }

    private void handleExit() {
        if (getActivity() == null) return;
        // Nếu có BackStack (chế độ edit), pop nó ra. Nếu không (mở từ Map), đóng activity.
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            requireActivity().finish();
        }
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_place_name);
        etAddress = view.findViewById(R.id.et_place_address);
        etDesc = view.findViewById(R.id.et_place_description);
        etLat = view.findViewById(R.id.et_lat);
        etLng = view.findViewById(R.id.et_lng);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        ivMainPreview = view.findViewById(R.id.iv_main_preview);
        tvPlaceholder = view.findViewById(R.id.tv_main_placeholder);
        tvFormTitle = view.findViewById(R.id.tv_form_title);
        etLat.setText(String.valueOf(lat));
        etLng.setText(String.valueOf(lng));
    }

    private void setupCategorySpinner() {
        String[] categories = categoryMap.keySet().toArray(new String[0]);
        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categories));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng initialPos = new LatLng(lat, lng);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 16f));
        currentMarker = mMap.addMarker(new MarkerOptions().position(initialPos).title("Vị trí ghim").draggable(true));
        mMap.setOnMapClickListener(this::updateMarkerPosition);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(@NonNull Marker m) {}
            @Override public void onMarkerDrag(@NonNull Marker m) {}
            @Override public void onMarkerDragEnd(@NonNull Marker m) { updateMarkerPosition(m.getPosition()); }
        });
        mMap.setOnCameraMoveStartedListener(reason -> {
            View mapV = getChildFragmentManager().findFragmentById(R.id.map_picker).getView();
            if (mapV != null) mapV.getParent().requestDisallowInterceptTouchEvent(true);
        });
    }

    private void updateMarkerPosition(LatLng latLng) {
        if (currentMarker != null) currentMarker.setPosition(latLng);
        lat = latLng.latitude; lng = latLng.longitude;
        etLat.setText(String.format("%.6f", lat)); etLng.setText(String.format("%.6f", lng));
    }

    private void savePlace() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String vnCat = spinnerCategory.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || vnCat.isEmpty()) {
            showSimpleDialog("Thiếu thông tin", "Vui lòng nhập đầy đủ các trường bắt buộc."); return;
        }

        Place.Request req = new Place.Request();
        req.name = name; req.address = address; req.latitude = lat; req.longitude = lng;
        req.description = etDesc.getText().toString().trim();
        req.category = categoryMap.get(vnCat);
        req.imageUrl = base64MainImage; req.imgSet = new ArrayList<>();

        showLoading();
        DataCallback<Place> callback = new DataCallback<Place>() {
            @Override
            public void onSuccess(Place data) {
                hideLoading();
                new AlertDialog.Builder(requireContext()).setTitle("Thành công")
                        .setMessage(isEditMode ? "Cập nhật địa điểm thành công!" : "Đã lưu địa điểm mới!")
                        .setPositiveButton("Xác nhận", (d, w) -> handleExit()).setCancelable(false).show();
            }
            @Override public void onError(String msg) { hideLoading(); showSimpleDialog("Lỗi hệ thống", msg); }
        };

        if (isEditMode) repository.updatePlace(mEditPlaceId, req, callback);
        else repository.createPlace(req, callback);
    }

    private void showSimpleDialog(String t, String m) {
        new AlertDialog.Builder(requireContext()).setTitle(t).setMessage(m).setPositiveButton("Đóng", null).show();
    }
}