package com.example.berotravel20.ui.main.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.utils.ImageUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private EditText etName, etBio;
    private ImageView ivAvatar, ivCover;
    private Button btnSave;
    private User currentUser;
    private OnProfileSaveListener listener;

    // Biến lưu chuỗi Base64 của ảnh mới (nếu có chọn)
    private String avatarBase64 = null;
    private String coverBase64 = null;

    // Interface truyền về đủ 4 món: Tên, Bio, Ảnh 1, Ảnh 2
    public interface OnProfileSaveListener {
        void onSave(String name, String bio, String avatarBase64, String coverBase64);
    }

    public EditProfileBottomSheet(User user, OnProfileSaveListener listener) {
        this.currentUser = user;
        this.listener = listener;
    }

    // 1. Bộ chọn ảnh Avatar
    private final ActivityResultLauncher<String> pickAvatar = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivAvatar.setImageURI(uri); // Hiển thị xem trước
                    avatarBase64 = ImageUtils.uriToBase64(requireContext(), uri); // Mã hóa luôn
                }
            }
    );

    // 2. Bộ chọn ảnh Bìa
    private final ActivityResultLauncher<String> pickCover = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivCover.setImageURI(uri);
                    coverBase64 = ImageUtils.uriToBase64(requireContext(), uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        etName = view.findViewById(R.id.etEditName);
        etBio = view.findViewById(R.id.etEditBio);
        ivAvatar = view.findViewById(R.id.ivEditAvatar);
        ivCover = view.findViewById(R.id.ivEditCover);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // Điền dữ liệu cũ vào để user mở ra là thấy
        if (currentUser != null) {
            etName.setText(currentUser.name);
            etBio.setText(currentUser.bio);
            if (currentUser.avatarUrl != null) Glide.with(this).load(currentUser.avatarUrl).into(ivAvatar);
            if (currentUser.coverUrl != null) Glide.with(this).load(currentUser.coverUrl).into(ivCover);
        }

        // Bắt sự kiện Click vào ảnh -> Mở thư viện chọn
        ivAvatar.setOnClickListener(v -> pickAvatar.launch("image/*"));
        ivCover.setOnClickListener(v -> pickCover.launch("image/*"));

        // Bắt sự kiện nút Lưu
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();

            if (listener != null) {
                // Gửi hàng về cho Fragment cha xử lý
                listener.onSave(newName, newBio, avatarBase64, coverBase64);
            }
            dismiss(); // Đóng dialog
        });
    }
}