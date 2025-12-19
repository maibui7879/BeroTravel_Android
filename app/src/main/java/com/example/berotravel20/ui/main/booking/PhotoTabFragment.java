package com.example.berotravel20.ui.main.booking;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.main.place.PhotoAdapter;
import com.example.berotravel20.utils.GridSpacingItemDecoration;
import com.example.berotravel20.utils.ImageUtils;
import java.util.ArrayList;

public class PhotoTabFragment extends BaseFragment {
    private String placeId, mainImage;
    private ArrayList<String> imgSet;
    private PhotoAdapter photoAdapter;
    private PlaceRepository placeRepository;

    private Uri selectedImageUri;
    private ImageView imgPreview;
    private Button btnUpload;
    private EditText etUrlInput;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (etUrlInput != null) etUrlInput.setText("");
                    if (imgPreview != null) {
                        imgPreview.setPadding(0, 0, 0, 0);
                        imgPreview.setAlpha(1.0f);
                        Glide.with(this).load(uri).into(imgPreview);
                    }
                    if (btnUpload != null) btnUpload.setEnabled(true);
                }
            }
    );

    public static PhotoTabFragment newInstance(String id, String mainImg, ArrayList<String> images) {
        PhotoTabFragment f = new PhotoTabFragment();
        Bundle b = new Bundle();
        b.putString("id", id);
        b.putString("mainImg", mainImg);
        b.putStringArrayList("imgs", images);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placeRepository = new PlaceRepository();
        if (getArguments() != null) {
            placeId = getArguments().getString("id");
            mainImage = getArguments().getString("mainImg");
            imgSet = getArguments().getStringArrayList("imgs");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_photo_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView(view);
        view.findViewById(R.id.btn_upload_photo).setOnClickListener(v -> showUploadDialog());
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_photos_booking);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        if (rv.getItemDecorationCount() == 0) {
            int spacing = (int) (12 * getResources().getDisplayMetrics().density);
            rv.addItemDecoration(new GridSpacingItemDecoration(3, spacing, true));
        }
        photoAdapter = new PhotoAdapter(pos -> showFullPhoto(pos));
        photoAdapter.setData(imgSet);
        rv.setAdapter(photoAdapter);
    }

    // --- HÀM RELOAD DỮ LIỆU TỪ SERVER ---
    private void refreshImageData() {
        showLoading();
        placeRepository.getPlaceById(placeId, new DataCallback<Place>() {
            @Override
            public void onSuccess(Place data) {
                hideLoading();
                if (data != null && data.imgSet != null) {
                    imgSet = new ArrayList<>(data.imgSet);
                    photoAdapter.setData(imgSet); // Cập nhật lại giao diện lưới ảnh
                }
            }

            @Override
            public void onError(String message) {
                hideLoading();
                showError("Không thể làm mới danh sách: " + message);
            }
        });
    }

    private void showUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_photo, null);
        builder.setView(dialogView);

        imgPreview = dialogView.findViewById(R.id.img_upload_preview);
        btnUpload = dialogView.findViewById(R.id.btn_dialog_upload);
        etUrlInput = dialogView.findViewById(R.id.et_photo_url);
        Button btnSelect = dialogView.findViewById(R.id.btn_select_image);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        etUrlInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    selectedImageUri = null;
                    btnUpload.setEnabled(true);
                    Glide.with(PhotoTabFragment.this).load(url).placeholder(R.drawable.placeholder_image).into(imgPreview);
                    imgPreview.setAlpha(1.0f);
                    imgPreview.setPadding(0, 0, 0, 0);
                }
            }
        });

        btnSelect.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpload.setOnClickListener(v -> {
            String urlInput = etUrlInput.getText().toString().trim();
            if (selectedImageUri != null) {
                processBase64Upload(selectedImageUri);
                dialog.dismiss();
            } else if (!urlInput.isEmpty()) {
                processUrlUpload(urlInput);
                dialog.dismiss();
            } else {
                showError("Vui lòng chọn ảnh hoặc nhập URL!");
            }
        });
        selectedImageUri = null;
        dialog.show();
    }

    private void processBase64Upload(Uri uri) {
        showLoading();
        // Ép kích thước ảnh xuống 800px để tránh lỗi 500 do dung lượng quá lớn
        String base64Image = ImageUtils.uriToBase64(requireContext(), uri);
        if (base64Image == null) {
            hideLoading();
            showError("Lỗi xử lý hình ảnh!");
            return;
        }
        updateServerImages(base64Image);
    }

    private void processUrlUpload(String url) {
        showLoading();
        updateServerImages(url);
    }

    private void updateServerImages(String newImageContent) {
        if (imgSet == null) imgSet = new ArrayList<>();
        ArrayList<String> backupList = new ArrayList<>(imgSet);
        imgSet.add(newImageContent);

        placeRepository.updatePlaceImages(placeId, mainImage, imgSet, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                showSuccess("Thêm ảnh thành công!");
                // TỰ ĐỘNG RELOAD ĐỂ ĐỒNG BỘ VỚI SERVER
                refreshImageData();
            }

            @Override
            public void onError(String message) {
                // Xử lý đặc biệt cho lỗi 500 nhưng thực tế đã lưu thành công
                if (message.contains("500")) {
                    showWarning("Đang đồng bộ lại dữ liệu...");
                    refreshImageData(); // Vẫn reload vì Server thường đã lưu xong
                } else {
                    hideLoading();
                    showError("Lỗi: " + message);
                    imgSet.clear();
                    imgSet.addAll(backupList);
                    photoAdapter.setData(imgSet);
                }
            }
        });
    }

    private void showFullPhoto(int position) {
        if (imgSet == null || position >= imgSet.size()) return;
        String url = imgSet.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(requireContext());
        Glide.with(this).load(url).into(iv);
        builder.setView(iv);
        AlertDialog dialog = builder.create();
        iv.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}