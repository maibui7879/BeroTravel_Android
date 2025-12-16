package com.example.berotravel20.ui.main.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.adapters.FavoriteAdapter;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.viewmodel.ProfileViewModel;

public class AccountFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvUserName, tvUserEmail, tvUserBio, tvFavoriteCount, tvReviewCount, tvTripCount, btnEditProfile;
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

        // Stats (Thống kê)
        tvReviewCount = view.findViewById(R.id.tvReviewCount);
        tvTripCount = view.findViewById(R.id.tvTripCount);
        tvFavoriteCount = view.findViewById(R.id.tvFavoriteCount);

        // List Favorites
        rvFavorites = view.findViewById(R.id.rvFavorites);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Setup RecyclerView (Adapter hiển thị danh sách yêu thích)
        favoriteAdapter = new FavoriteAdapter();
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(favoriteAdapter);

        // 3. Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        observeData(); // <-- Hàm quan trọng nhất ở đây

        // 4. Load Data (Chỉ cần gọi hàm này, nó sẽ tự gọi tiếp các hàm load stats bên trong ViewModel)
        viewModel.loadUserProfile();

        // 5. Events
        btnLogout.setOnClickListener(v -> handleLogout());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    // --- PHẦN QUAN TRỌNG: Lắng nghe dữ liệu thay đổi ---
    private void observeData() {
        // 1. Thông tin User (Tên, Email, Bio, Ảnh)
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText(user.name);
                if (user.email != null) {
                    tvUserEmail.setText(user.email);
                }

                if (user.bio != null && !user.bio.isEmpty()) {
                    tvUserBio.setText(user.bio);
                } else {
                    tvUserBio.setText("Chưa có giới thiệu.");
                }

                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    Glide.with(this).load(user.avatarUrl).placeholder(R.drawable.account_icon).into(ivAvatar);
                }

                if (user.coverUrl != null && !user.coverUrl.isEmpty()) {
                    Glide.with(this).load(user.coverUrl).placeholder(R.drawable.background).centerCrop().into(ivCover);
                }
            }
        });

        // 2. Danh sách địa điểm yêu thích (Cập nhật RecyclerView)
        viewModel.getFavoritePlaces().observe(getViewLifecycleOwner(), places -> {
            if (places != null) {
                favoriteAdapter.setPlaces(places); // Đổ dữ liệu vào Adapter
            }
        });

        // 3. Số lượng Yêu thích
        viewModel.getFavoriteCount().observe(getViewLifecycleOwner(), count -> {
            tvFavoriteCount.setText(String.valueOf(count));
        });

        // 4. Số lượng Chuyến đi (Trip)
        viewModel.getTripCount().observe(getViewLifecycleOwner(), count -> {
            tvTripCount.setText(String.valueOf(count));
        });

        // 5. Số lượng Đánh giá (Review)
        viewModel.getReviewCount().observe(getViewLifecycleOwner(), count -> {
            tvReviewCount.setText(String.valueOf(count));
        });
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

        EditProfileBottomSheet bottomSheet = new EditProfileBottomSheet(currentUser, (name, bio, avatarBase64, coverBase64) -> {
            String finalAvatar = (avatarBase64 != null) ? avatarBase64 : currentUser.avatarUrl;
            String finalCover = (coverBase64 != null) ? coverBase64 : currentUser.coverUrl;
            String dob = currentUser.dob;

            viewModel.updateProfile(name, bio, dob, finalAvatar, finalCover);
        });

        bottomSheet.show(getParentFragmentManager(), "EditProfileBottomSheet");
    }
}