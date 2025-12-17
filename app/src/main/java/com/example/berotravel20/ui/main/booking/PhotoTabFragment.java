package com.example.berotravel20.ui.main.booking;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    // Các biến phục vụ Dialog
    private Uri selectedImageUri;
    private ImageView imgPreview;
    private Button btnUpload;

    // Bộ lắng nghe chọn ảnh từ Gallery
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
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

        // Fix lỗi dính ảnh bằng ItemDecoration
        if (rv.getItemDecorationCount() == 0) {
            int spacing = (int) (12 * getResources().getDisplayMetrics().density);
            rv.addItemDecoration(new GridSpacingItemDecoration(3, spacing, true));
        }

        photoAdapter = new PhotoAdapter(pos -> showFullPhoto(pos));
        photoAdapter.setData(imgSet);
        rv.setAdapter(photoAdapter);
    }

    private void showUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_photo, null);
        builder.setView(dialogView);

        imgPreview = dialogView.findViewById(R.id.img_upload_preview);
        btnUpload = dialogView.findViewById(R.id.btn_dialog_upload);
        Button btnSelect = dialogView.findViewById(R.id.btn_select_image);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSelect.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpload.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageAsBase64(selectedImageUri);
                dialog.dismiss();
            }
        });

        selectedImageUri = null; // Reset URI mỗi lần mở dialog
        dialog.show();
    }

    private void uploadImageAsBase64(Uri uri) {
        showLoading();

        // 1. Sử dụng ImageUtils của bạn để chuyển sang Base64
        String base64Image = ImageUtils.uriToBase64(requireContext(), uri);

        if (base64Image == null) {
            hideLoading();
            showError("Lỗi xử lý hình ảnh!");
            return;
        }

        // 2. Cập nhật vào danh sách cục bộ
        if (imgSet == null) imgSet = new ArrayList<>();
        imgSet.add(base64Image);

        // 3. Gọi API update
        placeRepository.updatePlaceImages(placeId, mainImage, imgSet, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                showSuccess("Đăng ảnh thành công!");
                photoAdapter.setData(imgSet);
            }

            @Override
            public void onError(String message) {
                hideLoading();
                showError("Không thể upload: " + message);
                imgSet.remove(imgSet.size() - 1); // Hoàn tác nếu lỗi
            }
        });
    }

    private void showFullPhoto(int position) {
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