package com.example.berotravel20.ui.main.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.adapters.FavoriteAdapter; // Import Adapter mới
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.viewmodel.ProfileViewModel;

public class AccountFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvUserName,tvUserEmail, tvUserBio, tvFavoriteCount, tvReviewCount, tvTripCount, btnEditProfile;
    private ImageView ivAvatar, ivCover;
    private Button btnLogout;
    private RecyclerView rvFavorites;
    private FavoriteAdapter favoriteAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        ivCover = view.findViewById(R.id.ivCover);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserBio = view.findViewById(R.id.tvUserBio);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Stats
        tvReviewCount = view.findViewById(R.id.tvReviewCount);
        tvTripCount = view.findViewById(R.id.tvTripCount);
        tvFavoriteCount = view.findViewById(R.id.tvFavoriteCount);

        // List Favorites
        rvFavorites = view.findViewById(R.id.rvFavorites);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Setup RecyclerView
        favoriteAdapter = new FavoriteAdapter();
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(favoriteAdapter);

        // 3. Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        observeData();

        // 4. Load Data
        viewModel.loadUserProfile();
        viewModel.loadUserStats(); // Để lấy số lượng favorite

        // 5. Events
        btnLogout.setOnClickListener(v -> handleLogout());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void observeData() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText(user.name);
                if (user.email != null) {
                    tvUserEmail.setText(user.email);
                }
                // Hiển thị Bio (Nếu null thì hiện placeholder)
                if (user.bio != null && !user.bio.isEmpty()) {
                    tvUserBio.setText(user.bio);
                } else {
                    tvUserBio.setText("Chưa có giới thiệu.");
                }

                // Load Avatar
                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    Glide.with(this).load(user.avatarUrl).placeholder(R.drawable.account_icon).into(ivAvatar);
                }

                // Load Ảnh bìa (Cover)
                if (user.coverUrl != null && !user.coverUrl.isEmpty()) {
                    Glide.with(this).load(user.coverUrl).placeholder(R.drawable.background).centerCrop().into(ivCover);
                }
            }
        });

        // Lấy danh sách favorite IDs và hiển thị lên list (Tạm thời)
        // Vì hiện tại ViewModel chưa có LiveData cho List<String>, ta có thể chế thêm
        // Nhưng tạm thời ta dùng số lượng count để hiển thị Stats trước.

        viewModel.getFavoriteCount().observe(getViewLifecycleOwner(), count -> {
            tvFavoriteCount.setText(String.valueOf(count));
            // TODO: Khi nào có API trả về List<Place> thì update adapter ở đây
        });

        // Mock data cho Review và Trip (Vì chưa có API)
        tvReviewCount.setText("10");
        tvTripCount.setText("10");
    }

    private void handleLogout() {
        TokenManager.getInstance(requireContext()).clearToken();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showEditProfileDialog() {
        User currentUser = viewModel.getUser().getValue();
        if (currentUser == null) return;

        // Mở Dialog
        EditProfileBottomSheet bottomSheet = new EditProfileBottomSheet(currentUser, (name, bio, avatarBase64, coverBase64) -> {

            // Logic chọn cái gì để gửi:
            // 1. Nếu có ảnh mới (base64 khác null) -> Gửi base64
            // 2. Nếu không chọn ảnh mới -> Gửi lại URL cũ

            String finalAvatar = (avatarBase64 != null) ? avatarBase64 : currentUser.avatarUrl;
            String finalCover = (coverBase64 != null) ? coverBase64 : currentUser.coverUrl;

            // Ngày sinh giữ nguyên do lười làm layout =))
            String dob = currentUser.dob;

            // Gọi ViewModel
            viewModel.updateProfile(name, bio, dob, finalAvatar, finalCover);
        });

        bottomSheet.show(getParentFragmentManager(), "EditProfileBottomSheet");
    }
}