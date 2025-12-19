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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.Favorite.FavoriteResponse;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.repository.FavoriteRepository;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.common.RequestLoginDialog;
import com.example.berotravel20.ui.main.place.PlaceFragment;
import com.example.berotravel20.ui.map.MapActivity;
import com.example.berotravel20.utils.ToastUtils;
import com.example.berotravel20.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends BaseFragment implements RequestLoginDialog.RequestLoginListener {

    private ProfileViewModel viewModel;
    private FavoriteRepository favoriteRepository;

    // UI Components
    private TextView tvUserName, tvUserEmail, tvUserBio, tvFavoriteCount, tvReviewCount, tvTripCount, btnEditProfile;
    private ImageView ivAvatar, ivCover;
    private Button btnLogout;
    private RecyclerView rvFavorites;
    private View layoutEmptyState;

    // Sử dụng Adapter chung để đồng bộ giao diện
    private MapPlaceAdapter placeAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favoriteRepository = new FavoriteRepository();

        // 1. Kiểm tra trạng thái đăng nhập
        if (!isUserLoggedIn()) {
            showLoginRequestDialog();
            return;
        }

        // 2. Khởi tạo UI
        initViews(view);

        // 3. Thiết lập danh sách RecyclerView
        setupRecyclerView();

        // 4. Kết nối dữ liệu User qua ViewModel
        setupViewModel();

        // 5. Tải dữ liệu địa điểm yêu thích
        loadFavoriteData();

        // 6. Thiết lập các sự kiện click
        setupEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách mỗi khi quay lại tab Profile
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
        layoutEmptyState = view.findViewById(R.id.layout_empty_state); // Layout hiện khi danh sách trống
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupRecyclerView() {
        // Khởi tạo adapter với listener xử lý 3 hành động chính
        placeAdapter = new MapPlaceAdapter(getContext(), new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onFavoriteClick(Place place) {
                // Xử lý bỏ yêu thích trực tiếp
                handleFavoriteToggle(place);
            }

            @Override
            public void onItemClick(Place place) {
                // Điều hướng sang trang chi tiết địa điểm
                if (getActivity() instanceof BaseActivity) {
                    ((BaseActivity) getActivity()).navigateToDetail(PlaceFragment.newInstance(place.id));
                }
            }

            @Override
            public void onDirectionClick(Place place) {
                // Mở MapActivity ở chế độ chỉ đường từ vị trí hiện tại đến địa điểm này
                Intent intent = new Intent(requireContext(), MapActivity.class);
                intent.putExtra("ACTION_TYPE", "DIRECT_TO_PLACE");
                intent.putExtra("TARGET_LAT", place.latitude);
                intent.putExtra("TARGET_LNG", place.longitude);
                intent.putExtra("TARGET_NAME", place.name);
                startActivity(intent);
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(placeAdapter);
    }

    private void loadFavoriteData() {
        favoriteRepository.getMyFavorites(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> places) {
                if (isAdded() && places != null) {
                    // Cập nhật danh sách hiển thị
                    placeAdapter.setData(places);

                    // Logic quan trọng: Đánh dấu đỏ cho tất cả icon trái tim trong danh sách này
                    List<String> ids = new ArrayList<>();
                    for (Place p : places) ids.add(p.id);
                    placeAdapter.setFavoriteIds(ids);

                    // Cập nhật số lượng trên header
                    tvFavoriteCount.setText(String.valueOf(places.size()));

                    // Hiển thị Empty State nếu không có dữ liệu
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(places.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }

            @Override public void onError(String message) { }
        });
    }

    private void handleFavoriteToggle(Place place) {
        // Gọi API toggle. Ở màn hình Profile, hành động này tương đương với "Bỏ yêu thích"
        favoriteRepository.toggleFavorite(place.id, new DataCallback<FavoriteResponse>() {
            @Override
            public void onSuccess(FavoriteResponse data) {
                // Reload lại dữ liệu để item bị xóa khỏi danh sách ngay lập tức
                loadFavoriteData();
                showSuccess("Đã xóa khỏi danh sách yêu thích");
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvUserName.setText(user.name);
                if (user.email != null) tvUserEmail.setText(user.email);
                tvUserBio.setText((user.bio != null && !user.bio.isEmpty()) ? user.bio : "Khám phá thế giới cùng BeroTravel.");

                // Load ảnh Avatar và Cover
                Glide.with(this).load(user.avatarUrl).placeholder(R.drawable.account_icon).circleCrop().into(ivAvatar);
                if (user.coverUrl != null) {
                    Glide.with(this).load(user.coverUrl).placeholder(R.drawable.background).centerCrop().into(ivCover);
                }
            }
        });

        viewModel.loadUserProfile();
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

    private void showLoginRequestDialog() {
        RequestLoginDialog dialog = RequestLoginDialog.newInstance();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "RequestLoginDialog");
    }

    @Override public void onLoginClick() {
        startActivity(new Intent(requireContext(), AuthActivity.class));
    }

    @Override public void onCancelClick() { }
}