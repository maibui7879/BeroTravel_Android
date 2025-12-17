package com.example.berotravel20.ui.main.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.berotravel20.adapters.FavoriteAdapter;
import com.example.berotravel20.data.common.DataCallback; // Import Callback
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.Place.Place; // Import Place
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.repository.FavoriteRepository; // Import Repo
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.ui.common.RequestLoginDialog;
import com.example.berotravel20.ui.map.MapActivity;
import com.example.berotravel20.viewmodel.ProfileViewModel;

import java.util.List;

public class AccountFragment extends Fragment implements RequestLoginDialog.RequestLoginListener {

    private ProfileViewModel viewModel;

    // Repository để lấy danh sách yêu thích
    private FavoriteRepository favoriteRepository;

    // UI Components
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

        // 1. Khởi tạo Repository
        favoriteRepository = new FavoriteRepository();

        // 2. KIỂM TRA ĐĂNG NHẬP
        if (!isUserLoggedIn()) {
            showLoginRequestDialog();
            return;
        }

        // 3. Ánh xạ View
        initViews(view);

        // 4. Setup RecyclerView & Adapter
        setupRecyclerView();

        // 5. Setup ViewModel (Cho thông tin User cơ bản)
        setupViewModel();

        // 6. [QUAN TRỌNG] Tải danh sách yêu thích từ API
        loadFavoriteData();

        // 7. Setup Events
        setupEvents();
    }

    // [MỚI] Khi quay lại tab này (ví dụ từ Map về), reload lại danh sách để cập nhật nếu có thay đổi
    @Override
    public void onResume() {
        super.onResume();
        if (isUserLoggedIn()) {
            loadFavoriteData();
        }
    }

    private void initViews(View view) {
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
    }

    private void setupRecyclerView() {
        favoriteAdapter = new FavoriteAdapter();

        // Listener xử lý sự kiện click
        favoriteAdapter.setListener(new FavoriteAdapter.OnFavoriteActionListener() {
            @Override
            public void onPlaceClick(Place place) {
                // Xử lý xem chi tiết
                Toast.makeText(getContext(), "Chi tiết: " + place.name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDirectionClick(Place place) {
                // Chuyển sang MapActivity với chế độ chỉ đường
                Intent intent = new Intent(requireContext(), MapActivity.class);
                intent.putExtra("ACTION_TYPE", "DIRECT_TO_PLACE");
                intent.putExtra("TARGET_LAT", place.latitude);
                intent.putExtra("TARGET_LNG", place.longitude);
                intent.putExtra("TARGET_NAME", place.name);
                intent.putExtra("TARGET_ADDR", place.address);
                startActivity(intent);
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(favoriteAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Chỉ quan sát thông tin User (Tên, Avatar, Bio...)
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText(user.name);
                if (user.email != null) tvUserEmail.setText(user.email);
                tvUserBio.setText((user.bio != null && !user.bio.isEmpty()) ? user.bio : "Chưa có giới thiệu.");

                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    Glide.with(this).load(user.avatarUrl).placeholder(R.drawable.account_icon).into(ivAvatar);
                }
                if (user.coverUrl != null && !user.coverUrl.isEmpty()) {
                    Glide.with(this).load(user.coverUrl).placeholder(R.drawable.background).centerCrop().into(ivCover);
                }
            }
        });

        // Load thông tin user
        viewModel.loadUserProfile();
    }

    // [HÀM MỚI] Gọi API lấy danh sách yêu thích và hiển thị lên RecyclerView
    private void loadFavoriteData() {
        favoriteRepository.getMyFavorites(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> places) {
                if (places != null) {
                    // Cập nhật Adapter
                    if (favoriteAdapter != null) {
                        favoriteAdapter.setPlaces(places);
                    }
                    // Cập nhật số lượng trên UI
                    if (tvFavoriteCount != null) {
                        tvFavoriteCount.setText(String.valueOf(places.size()));
                    }
                }
            }

            @Override
            public void onError(String message) {
                // Có thể log lỗi hoặc hiện Toast nếu cần thiết
                // Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEvents() {
        btnLogout.setOnClickListener(v -> handleLogout());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
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

        EditProfileBottomSheet bottomSheet = new EditProfileBottomSheet(currentUser, (name, bio, dob, avatarBase64, coverBase64) -> {
            String finalAvatar = (avatarBase64 != null) ? avatarBase64 : currentUser.avatarUrl;
            String finalCover = (coverBase64 != null) ? coverBase64 : currentUser.coverUrl;
            viewModel.updateProfile(name, bio, dob, finalAvatar, finalCover);
        });

        bottomSheet.show(getParentFragmentManager(), "EditProfileBottomSheet");
    }

    // --- LOGIC AUTH ---
    private boolean isUserLoggedIn() {
        if (getContext() == null) return false;
        String token = TokenManager.getInstance(requireContext()).getToken();
        return token != null && !token.isEmpty();
    }

    private void showLoginRequestDialog() {
        RequestLoginDialog dialog = RequestLoginDialog.newInstance();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "RequestLoginDialog");
    }

    @Override
    public void onLoginClick() {
        if (getActivity() != null) {
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCancelClick() {
        // Tùy chọn: Xử lý khi user hủy dialog
    }
}