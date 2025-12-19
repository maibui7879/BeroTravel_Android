package com.example.berotravel20.ui.map;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.utils.CategoryUtils;
import com.example.berotravel20.utils.ImageUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddPlaceFragment extends BaseFragment implements OnMapReadyCallback {
    private double lat, lng;
    private String mEditPlaceId = null;
    private boolean isEditMode = false;
    private PlaceRepository repository;
    private String base64MainImage = null;

    private EditText etName, etAddress, etDesc, etLat, etLng, etImageUrl;
    private AutoCompleteTextView spinnerCategory;
    private ImageView ivMainPreview;
    private TextView tvPlaceholder, tvFormTitle;
    private GoogleMap mMap;
    private Marker currentMarker;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    ivMainPreview.setImageURI(uri);
                    tvPlaceholder.setVisibility(View.GONE);
                    etImageUrl.setText(""); // Xóa URL nếu chọn ảnh từ máy
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
        setupUrlImageListener();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_picker);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        if (isEditMode) {
            tvFormTitle.setText("Chỉnh sửa địa điểm");
            loadExistingPlaceData();
        }

        view.findViewById(R.id.btn_select_main_image).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        view.findViewById(R.id.btn_save_place).setOnClickListener(v -> savePlace());

        View.OnClickListener closeAction = v -> handleExit();
        view.findViewById(R.id.btn_cancel).setOnClickListener(closeAction);
        view.findViewById(R.id.btn_close).setOnClickListener(closeAction);
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_place_name);
        etAddress = view.findViewById(R.id.et_place_address);
        etDesc = view.findViewById(R.id.et_place_description);
        etLat = view.findViewById(R.id.et_lat);
        etLng = view.findViewById(R.id.et_lng);
        etImageUrl = view.findViewById(R.id.et_image_url);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        ivMainPreview = view.findViewById(R.id.iv_main_preview);
        tvPlaceholder = view.findViewById(R.id.tv_main_placeholder);
        tvFormTitle = view.findViewById(R.id.tv_form_title);
        etLat.setText(String.valueOf(lat));
        etLng.setText(String.valueOf(lng));
    }

    private void setupCategorySpinner() {
        // Lấy toàn bộ danh sách tên Tiếng Việt từ CategoryUtils
        List<String> displayNames = new ArrayList<>(CategoryUtils.CATEGORY_MAP.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, displayNames);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupUrlImageListener() {
        etImageUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    base64MainImage = null;
                    tvPlaceholder.setVisibility(View.GONE);
                    Glide.with(AddPlaceFragment.this).load(url).into(ivMainPreview);
                }
            }
        });
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
                etImageUrl.setText(place.imageUrl);

                // Hiển thị tên Tiếng Việt dựa trên key từ Server
                spinnerCategory.setText(CategoryUtils.getLabel(place.category), false);

                if (place.imageUrl != null) {
                    tvPlaceholder.setVisibility(View.GONE);
                    Glide.with(AddPlaceFragment.this).load(place.imageUrl).into(ivMainPreview);
                }
            }
            @Override public void onError(String msg) { hideLoading(); showCustomDialog("Lỗi", msg, false, null); }
        });
    }

    private void savePlace() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String vnNameSelected = spinnerCategory.getText().toString().trim();
        String inputUrl = etImageUrl.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || vnNameSelected.isEmpty()) {
            showCustomDialog("Thiếu thông tin", "Vui lòng nhập đầy đủ các trường bắt buộc.", false, null);
            return;
        }

        // Tìm lại key server dựa trên giá trị hiển thị tiếng Việt
        String serverKey = "other"; // mặc định
        for (Map.Entry<String, String> entry : CategoryUtils.CATEGORY_MAP.entrySet()) {
            if (entry.getValue().equals(vnNameSelected)) {
                serverKey = entry.getKey();
                break;
            }
        }

        Place.Request req = new Place.Request();
        req.name = name; req.address = address; req.latitude = lat; req.longitude = lng;
        req.description = etDesc.getText().toString().trim();
        req.category = serverKey;

        if (base64MainImage != null) req.imageUrl = base64MainImage;
        else if (!inputUrl.isEmpty()) req.imageUrl = inputUrl;

        req.imgSet = new ArrayList<>();

        showLoading();
        DataCallback<Place> callback = new DataCallback<Place>() {
            @Override
            public void onSuccess(Place data) {
                hideLoading();
                String msg = isEditMode ? "Cập nhật thành công!" : "Đã lưu địa điểm mới!";
                showCustomDialog("Thành công", msg, true, () -> handleExit());
            }
            @Override public void onError(String msg) { hideLoading(); showCustomDialog("Lỗi", msg, false, null); }
        };

        if (isEditMode) repository.updatePlace(mEditPlaceId, req, callback);
        else repository.createPlace(req, callback);
    }

    private void handleExit() {
        if (getActivity() == null) return;
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            requireActivity().finish();
        }
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
    }

    private void updateMarkerPosition(LatLng latLng) {
        if (currentMarker != null) currentMarker.setPosition(latLng);
        lat = latLng.latitude; lng = latLng.longitude;
        etLat.setText(String.format("%.6f", lat));
        etLng.setText(String.format("%.6f", lng));
    }

    private void showCustomDialog(String title, String message, boolean isSuccess, Runnable onConfirm) {
        if (getActivity() == null) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog_success, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create();

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvMsg = dialogView.findViewById(R.id.tv_dialog_message);
        ImageView ivIcon = dialogView.findViewById(R.id.iv_dialog_icon);
        Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);

        tvTitle.setText(title); tvMsg.setText(message);
        if (!isSuccess) {
            ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.RED));
            btnConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
        }
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        });
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }
        dialog.show();
    }
}