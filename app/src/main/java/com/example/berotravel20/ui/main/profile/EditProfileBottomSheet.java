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

    // [SỬA 1] Thêm khai báo etDob vào đây
    private EditText etName, etBio, etDob;
    private ImageView ivAvatar, ivCover;
    private Button btnSave;

    private User currentUser;
    private OnProfileSaveListener listener;

    private String avatarBase64 = null;
    private String coverBase64 = null;

    public interface OnProfileSaveListener {
        void onSave(String name, String bio, String dob, String avatarBase64, String coverBase64);
    }

    public EditProfileBottomSheet(User user, OnProfileSaveListener listener) {
        this.currentUser = user;
        this.listener = listener;
    }

    private final ActivityResultLauncher<String> pickAvatar = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ivAvatar.setImageURI(uri);
                    avatarBase64 = ImageUtils.uriToBase64(requireContext(), uri);
                }
            }
    );

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
        // Đảm bảo tên layout đúng với tên file XML bạn tạo (trong ví dụ XML bạn gửi ko có tên file, mình giả định là layout_edit_profile)
        return inflater.inflate(R.layout.layout_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

            // [SỬA 2] Fill ngày sinh cũ nếu có
            if (currentUser.dob != null) {
                etDob.setText(currentUser.dob);
            }

            if (currentUser.avatarUrl != null) Glide.with(this).load(currentUser.avatarUrl).into(ivAvatar);
            if (currentUser.coverUrl != null) Glide.with(this).load(currentUser.coverUrl).into(ivCover);
        }

        // Sự kiện chọn ảnh
        ivAvatar.setOnClickListener(v -> pickAvatar.launch("image/*"));
        ivCover.setOnClickListener(v -> pickCover.launch("image/*"));

        // [SỬA 3] Sự kiện chọn Ngày sinh (DatePicker)
        // Vì XML có focusable="false" nên bắt buộc phải dùng onClick để hiện Dialog chọn ngày
        etDob.setOnClickListener(v -> showDatePickerDialog());

        // Sự kiện Lưu
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();
            String newDob = etDob.getText().toString().trim();

            if (listener != null) {
                listener.onSave(newName, newBio, newDob, avatarBase64, coverBase64);
            }
            dismiss();
        });
    }

    // [SỬA 4] Hàm hiển thị lịch
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format lại ngày tháng thành DD/MM/YYYY
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDob.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }
}