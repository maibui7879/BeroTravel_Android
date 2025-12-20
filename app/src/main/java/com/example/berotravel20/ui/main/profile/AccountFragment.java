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
import androidx.appcompat.app.AlertDialog; // [MỚI]
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.Favorite.FavoriteResponse;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Stats.UserStatsResponse;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.repository.FavoriteRepository;
import com.example.berotravel20.data.repository.JourneyRepository;
import com.example.berotravel20.data.repository.UserStatsRepository;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.common.RequestLoginDialog;
import com.example.berotravel20.ui.main.place.PlaceFragment;
import com.example.berotravel20.ui.map.MapActivity;
import com.example.berotravel20.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends BaseFragment implements RequestLoginDialog.RequestLoginListener {

    private ProfileViewModel viewModel;
    private User mCurrentUser; // [MỚI] Biến lưu user hiện tại để lấy URL ảnh khi click

    private FavoriteRepository favoriteRepository;
    private UserStatsRepository userStatsRepository;
    private JourneyRepository journeyRepository;

    private TextView tvUserName, tvUserEmail, tvUserBio, tvFavoriteCount, tvReviewCount, tvTripCount, btnEditProfile;
    private ImageView ivAvatar, ivCover;
    private Button btnLogout;
    private RecyclerView rvFavorites;
    private View layoutEmptyState;

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
        userStatsRepository = new UserStatsRepository();
        journeyRepository = new JourneyRepository(requireContext());

        if (!isUserLoggedIn()) {
            showLoginRequestDialog();
            return;
        }

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        refreshData();
        setupEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUserLoggedIn()) {
            refreshData();
        }
    }

    private void refreshData() {
        loadFavoriteData();
        loadUserStats();
        loadTripCount();
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
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupRecyclerView() {
        placeAdapter = new MapPlaceAdapter(getContext(), new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onFavoriteClick(Place place) {
                handleFavoriteToggle(place);
            }

            @Override
            public void onItemClick(Place place) {
                if (getActivity() instanceof BaseActivity) {
                    ((BaseActivity) getActivity()).navigateToDetail(PlaceFragment.newInstance(place.id));
                }
            }

            @Override
            public void onDirectionClick(Place place) {
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
                    placeAdapter.setData(places);
                    List<String> ids = new ArrayList<>();
                    for (Place p : places) ids.add(p.id);
                    placeAdapter.setFavoriteIds(ids);
                    tvFavoriteCount.setText(String.valueOf(places.size()));
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(places.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            }
            @Override public void onError(String message) { }
        });
    }

    private void loadUserStats() {
        userStatsRepository.getMyStats(new DataCallback<UserStatsResponse>() {
            @Override
            public void onSuccess(UserStatsResponse data) {
                if (isAdded() && data != null && data.reviewsCreated != null) {
                    tvReviewCount.setText(String.valueOf(data.reviewsCreated.count));
                }
            }
            @Override public void onError(String message) {}
        });
    }

    private void loadTripCount() {
        journeyRepository.getJourneys(new DataCallback<List<Journey>>() {
            @Override
            public void onSuccess(List<Journey> data) {
                if (isAdded()) {
                    int count = (data != null) ? data.size() : 0;
                    tvTripCount.setText(String.valueOf(count));
                }
            }
            @Override public void onError(String message) {}
        });
    }

    private void handleFavoriteToggle(Place place) {
        favoriteRepository.toggleFavorite(place.id, new DataCallback<FavoriteResponse>() {
            @Override
            public void onSuccess(FavoriteResponse data) {
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
                mCurrentUser = user; // [MỚI] Lưu user vào biến toàn cục

                tvUserName.setText(user.name);
                if (user.email != null) tvUserEmail.setText(user.email);
                tvUserBio.setText((user.bio != null && !user.bio.isEmpty()) ? user.bio : "Thành viên BeroTravel.");
                Glide.with(this).load(user.avatarUrl).placeholder(R.drawable.account_icon).circleCrop().into(ivAvatar);
                if (user.coverUrl != null) {
                    Glide.with(this).load(user.coverUrl).placeholder(R.drawable.bg_top).centerCrop().into(ivCover);
                }
            }
        });
        viewModel.loadUserProfile();
    }

    private void setupEvents() {
        btnLogout.setOnClickListener(v -> handleLogout());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // [MỚI] Thêm sự kiện click vào Avatar để xem full
        ivAvatar.setOnClickListener(v -> {
            if (mCurrentUser != null && mCurrentUser.avatarUrl != null && !mCurrentUser.avatarUrl.isEmpty()) {
                showFullImage(mCurrentUser.avatarUrl);
            }
        });

        // [MỚI] Thêm sự kiện click vào Ảnh bìa (Cover) để xem full (nếu muốn)
        ivCover.setOnClickListener(v -> {
            if (mCurrentUser != null && mCurrentUser.coverUrl != null && !mCurrentUser.coverUrl.isEmpty()) {
                showFullImage(mCurrentUser.coverUrl);
            }
        });
    }

    // [MỚI] Hàm hiển thị ảnh Fullscreen (Tái sử dụng layout dialog_view_photo.xml)
    private void showFullImage(String url) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_view_photo, null);
        builder.setView(view);

        ImageView ivFull = view.findViewById(R.id.iv_full_photo);
        View btnClose = view.findViewById(R.id.btn_close_photo);

        Glide.with(this)
                .load(url)
                .fitCenter()
                .placeholder(R.drawable.placeholder_image)
                .into(ivFull);

        AlertDialog dialog = builder.create();
        btnClose.setOnClickListener(v -> dialog.dismiss());
        ivFull.setOnClickListener(v -> dialog.dismiss()); // Bấm vào ảnh cũng đóng
        dialog.show();
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
        EditProfileBottomSheet bottomSheet = new EditProfileBottomSheet(currentUser, (name, bio, dob, avatarUrl, coverUrl) -> {
            String finalAvatar = (avatarUrl != null) ? avatarUrl : currentUser.avatarUrl;
            String finalCover = (coverUrl != null) ? coverUrl : currentUser.coverUrl;
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

    @Override public void onCancelClick() {

    }
}