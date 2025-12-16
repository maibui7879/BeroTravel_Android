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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private EditText etName, etBio;
    private ImageView ivAvatar, ivCover;
    private Button btnSave;

    // Biến lưu Uri ảnh tạm thời
    private Uri avatarUri, coverUri;
    private User currentUser;

    // Interface để báo về cho AccountFragment cập nhật lại UI
    private OnProfileUpdatedListener listener;

    public interface OnProfileUpdatedListener {
        void onProfileUpdated();
    }

    public EditProfileBottomSheet(User user, OnProfileUpdatedListener listener) {
        this.currentUser = user;
        this.listener = listener;
    }

    // Launcher chọn Avatar
    private final ActivityResultLauncher<String> pickAvatar = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    avatarUri = uri;
                    ivAvatar.setImageURI(uri); // Hiển thị xem trước
                }
            }
    );

    // Launcher chọn Cover
    private final ActivityResultLauncher<String> pickCover = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    coverUri = uri;
                    ivCover.setImageURI(uri); // Hiển thị xem trước
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

        // 1. Ánh xạ
        etName = view.findViewById(R.id.etEditName);
        etBio = view.findViewById(R.id.etEditBio);
        ivAvatar = view.findViewById(R.id.ivEditAvatar);
        ivCover = view.findViewById(R.id.ivEditCover);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // 2. Điền dữ liệu cũ vào
        if (currentUser != null) {
            etName.setText(currentUser.name);
            etBio.setText(currentUser.bio);
            if (currentUser.avatarUrl != null) Glide.with(this).load(currentUser.avatarUrl).into(ivAvatar);
            if (currentUser.coverUrl != null) Glide.with(this).load(currentUser.coverUrl).into(ivCover);
        }

        // 3. Bắt sự kiện chọn ảnh
        ivAvatar.setOnClickListener(v -> pickAvatar.launch("image/*"));
        ivCover.setOnClickListener(v -> pickCover.launch("image/*"));

        // 4. Bắt sự kiện Lưu
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();

            // TODO: Ở đây m cần gọi ViewModel để upload Multipart
            // Vì xử lý File Path trong Android khá loằng ngoằng,
            // tạm thời t show Toast thông báo logic.

            Toast.makeText(getContext(), "Đang cập nhật... (Cần code Multipart)", Toast.LENGTH_SHORT).show();

            // Giả lập thành công để đóng Dialog
            dismiss();
            if (listener != null) listener.onProfileUpdated();
        });
    }
}