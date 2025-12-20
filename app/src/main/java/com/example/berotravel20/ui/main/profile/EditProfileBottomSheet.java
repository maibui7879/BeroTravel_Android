package com.example.berotravel20.ui.main.profile;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.Calendar;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private EditText etName, etBio, etDob;
    private ImageView ivAvatar, ivCover;
    private Button btnSave;
    private ProgressBar progressBar; // Thêm progress bar để báo hiệu đang upload

    private User currentUser;
    private OnProfileSaveListener listener;

    // Các biến này giờ sẽ chứa LINK URL từ Cloudinary thay vì chuỗi Base64
    private String avatarUrl = null;
    private String coverUrl = null;

    public interface OnProfileSaveListener {
        void onSave(String name, String bio, String dob, String avatarUrl, String coverUrl);
    }

    public EditProfileBottomSheet(User user, OnProfileSaveListener listener) {
        this.currentUser = user;
        this.listener = listener;
    }

    // Xử lý chọn Avatar
    private final ActivityResultLauncher<String> pickAvatar = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivAvatar.setImageURI(uri);
                    if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                    btnSave.setEnabled(false); // Vô hiệu hóa nút Lưu khi đang upload

                    ImageUtils.uriToBase64(requireContext(), uri, new ImageUtils.CloudinaryCallback() {
                        @Override
                        public void onSuccess(String url) {
                            avatarUrl = url;
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    btnSave.setEnabled(true);
                                    Toast.makeText(getContext(), "Tải ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    btnSave.setEnabled(true);
                                    Toast.makeText(getContext(), "Lỗi tải ảnh: " + error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                }
            }
    );

    // Xử lý chọn Cover (Ảnh bìa)
    private final ActivityResultLauncher<String> pickCover = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivCover.setImageURI(uri);
                    if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                    btnSave.setEnabled(false);

                    ImageUtils.uriToBase64(requireContext(), uri, new ImageUtils.CloudinaryCallback() {
                        @Override
                        public void onSuccess(String url) {
                            coverUrl = url;
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    btnSave.setEnabled(true);
                                    Toast.makeText(getContext(), "Tải ảnh bìa thành công", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    btnSave.setEnabled(true);
                                    Toast.makeText(getContext(), "Lỗi tải ảnh bìa: " + error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
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

        // Khởi tạo Cloudinary nếu chưa làm ở MainActivity
        ImageUtils.initCloudinary();

        etDob = view.findViewById(R.id.etEditDob);
        etName = view.findViewById(R.id.etEditName);
        etBio = view.findViewById(R.id.etEditBio);
        ivAvatar = view.findViewById(R.id.ivEditAvatar);
        ivCover = view.findViewById(R.id.ivEditCover);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // Điền dữ liệu cũ
        if (currentUser != null) {
            etName.setText(currentUser.name);
            etBio.setText(currentUser.bio);
            if (currentUser.dob != null) etDob.setText(currentUser.dob);

            // Giữ lại URL cũ nếu người dùng không thay đổi ảnh mới
            avatarUrl = currentUser.avatarUrl;
            coverUrl = currentUser.coverUrl;

            if (currentUser.avatarUrl != null) Glide.with(this).load(currentUser.avatarUrl).into(ivAvatar);
            if (currentUser.coverUrl != null) Glide.with(this).load(currentUser.coverUrl).into(ivCover);
        }

        ivAvatar.setOnClickListener(v -> pickAvatar.launch("image/*"));
        ivCover.setOnClickListener(v -> pickCover.launch("image/*"));
        etDob.setOnClickListener(v -> showDatePickerDialog());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();
            String newDob = etDob.getText().toString().trim();

            if (listener != null) {
                // Trả về link URL thay vì Base64
                listener.onSave(newName, newBio, newDob, avatarUrl, coverUrl);
            }
            dismiss();
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDob.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }
}