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
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.ui.common.RequestLoginDialog; // Đảm bảo đã tạo file này
import com.example.berotravel20.viewmodel.ProfileViewModel;

// [MỚI] Implement interface để lắng nghe sự kiện từ Dialog yêu cầu đăng nhập
public class AccountFragment extends Fragment implements RequestLoginDialog.RequestLoginListener {

    private ProfileViewModel viewModel;
    private TextView tvUserName, tvUserEmail, tvUserBio, tvFavoriteCount, tvReviewCount, tvTripCount, btnEditProfile;
    private ImageView ivAvatar, ivCover;
    private Button btnLogout;
    private RecyclerView rvFavorites;
    private FavoriteAdapter favoriteAdapter;

    // View chứa nội dung (để ẩn đi nếu chưa login - tùy chọn)
    private View contentLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. KIỂM TRA ĐĂNG NHẬP NGAY LẬP TỨC
        if (!isUserLoggedIn()) {
            showLoginRequestDialog();
            return; // Dừng lại, không chạy code bên dưới (tránh crash)
        }

        // --- NẾU ĐÃ LOGIN THÌ CHẠY TIẾP ---

        // 2. Ánh xạ View
        ivCover = view.findViewById(R.id.ivCover);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserBio = view.findViewById(R.id.tvUserBio);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        tvReviewCount = view.findViewById(R.id.tvReviewCount);
        tvTripCount = view.findViewById(R.id.tvTripCount);
        tvFavoriteCount = view.findViewById(R.id.tvFavoriteCount);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 3. Setup RecyclerView
        favoriteAdapter = new FavoriteAdapter();
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(favoriteAdapter);

        // 4. Setup ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        observeData();
        viewModel.loadUserProfile();

        // 5. Events
        btnLogout.setOnClickListener(v -> handleLogout());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    // --- CÁC HÀM KIỂM TRA LOGIN ---
    private boolean isUserLoggedIn() {
        String token = TokenManager.getInstance(requireContext()).getToken();
        return token != null && !token.isEmpty();
    }

    private void showLoginRequestDialog() {
        RequestLoginDialog dialog = RequestLoginDialog.newInstance();
        dialog.setListener(this); // Gắn listener vào Fragment này
        dialog.show(getChildFragmentManager(), "RequestLoginDialog");
    }

    // Xử lý khi user bấm "Đăng nhập ngay" trên Dialog
    @Override
    public void onLoginClick() {
        if (getActivity() != null) {
            getActivity().finish(); // Đóng màn hình hiện tại để sang AuthActivity sạch sẽ
        }
    }

    // Xử lý khi user bấm "Quay lại" trên Dialog
    @Override
    public void onCancelClick() {
        // Quay về Home hoặc tab đầu tiên
        if (getActivity() instanceof BaseActivity) {
            // Giả sử BaseActivity có ViewPager hoặc hàm chuyển tab
            // ((BaseActivity) getActivity()).navigateToHome();
            // Nếu chưa có hàm đó, tạm thời không làm gì hoặc gọi:
            // getActivity().onBackPressed();
        }
    }
    // ------------------------------------

    private void observeData() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText(user.name);
                if (user.email != null) tvUserEmail.setText(user.email);

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

        viewModel.getFavoritePlaces().observe(getViewLifecycleOwner(), places -> {
            if (places != null) {
                favoriteAdapter.setPlaces(places);
            }
        });

        viewModel.getFavoriteCount().observe(getViewLifecycleOwner(), count -> tvFavoriteCount.setText(String.valueOf(count)));
        viewModel.getTripCount().observe(getViewLifecycleOwner(), count -> tvTripCount.setText(String.valueOf(count)));
        viewModel.getReviewCount().observe(getViewLifecycleOwner(), count -> tvReviewCount.setText(String.valueOf(count)));
    }

    private void handleLogout() {
        TokenManager.getInstance(requireContext()).clearSession();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showEditProfileDialog() {
        User currentUser = viewModel.getUser().getValue();
        if (currentUser == null) return;

        // [QUAN TRỌNG] Đảm bảo bạn ĐÃ SỬA EditProfileBottomSheet để interface nhận 5 tham số
        EditProfileBottomSheet bottomSheet = new EditProfileBottomSheet(currentUser, (name, bio, dob, avatarBase64, coverBase64) -> {
            // Logic xử lý khi bấm Save
            String finalAvatar = (avatarBase64 != null) ? avatarBase64 : currentUser.avatarUrl;
            String finalCover = (coverBase64 != null) ? coverBase64 : currentUser.coverUrl;

            // Bây giờ 'dob' là ngày sinh MỚI từ dialog
            viewModel.updateProfile(name, bio, dob, finalAvatar, finalCover);
        });

        bottomSheet.show(getParentFragmentManager(), "EditProfileBottomSheet");
    }
}