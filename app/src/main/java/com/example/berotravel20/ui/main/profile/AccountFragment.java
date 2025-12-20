package com.example.berotravel20.ui.main.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.api.UserApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.data.model.Favorite.FavoriteResponse;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Stats.UserStatsResponse;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.remote.RetrofitClient;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends BaseFragment implements RequestLoginDialog.RequestLoginListener {

    private ProfileViewModel viewModel;
    private User mCurrentUser;

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

        TextView btnChangePassword = view.findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
    }

    // --- LOGIC ĐỔI MẬT KHẨU ---

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etOld = dialogView.findViewById(R.id.etOldPassword);
        EditText etNew = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String oldP = etOld.getText().toString().trim();
            String newP = etNew.getText().toString().trim();
            String confirmP = etConfirm.getText().toString().trim();

            if (oldP.isEmpty() || newP.isEmpty() || confirmP.isEmpty()) {
                showCustomNotifyDialog("Thiếu thông tin", "Vui lòng nhập đầy đủ 3 ô mật khẩu", false);
                return;
            }
            if (oldP.equals(newP)) {
                showCustomNotifyDialog("Trùng mật khẩu", "Mật khẩu mới phải khác mật khẩu hiện tại", false);
                return;
            }
            if (!newP.equals(confirmP)) {
                showCustomNotifyDialog("Lỗi xác nhận", "Mật khẩu xác nhận không trùng khớp", false);
                return;
            }
            if (newP.length() < 6) {
                showCustomNotifyDialog("Mật khẩu yếu", "Mật khẩu mới phải có ít nhất 6 ký tự", false);
                return;
            }

            User userUpdate = new User();
            userUpdate.setPassword(newP);

            UserApiService apiService = RetrofitClient.getInstance(getContext()).getRetrofit().create(UserApiService.class);
            apiService.changePassword(userUpdate).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        showCustomNotifyDialog("Thành công", "Mật khẩu của bạn đã được thay đổi!", true);
                    } else {
                        showCustomNotifyDialog("Thất bại", "Mật khẩu cũ không chính xác hoặc lỗi hệ thống", false);
                    }
                }
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    showCustomNotifyDialog("Lỗi kết nối", "Không thể liên lạc với máy chủ", false);
                }
            });
        });
        dialog.show();
    }

    private void showCustomNotifyDialog(String title, String message, boolean isSuccess) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog_success, null);
        AlertDialog notifyDialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvMsg = dialogView.findViewById(R.id.tv_dialog_message);
        ImageView ivIcon = dialogView.findViewById(R.id.iv_dialog_icon);
        Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);

        tvTitle.setText(title);
        tvMsg.setText(message);

        if (!isSuccess) {
            ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
            btnConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
        }

        btnConfirm.setOnClickListener(v -> notifyDialog.dismiss());
        if (notifyDialog.getWindow() != null) notifyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        notifyDialog.show();
    }

    // --- LOGIC HIỂN THỊ DỮ LIỆU ---
    private void setupRecyclerView() {
        placeAdapter = new MapPlaceAdapter(getContext(), new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onFavoriteClick(Place place) { handleFavoriteToggle(place); }

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
            public void onError(String message) { showError(message); }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                mCurrentUser = user;
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

        ivAvatar.setOnClickListener(v -> {
            if (mCurrentUser != null && mCurrentUser.avatarUrl != null) showFullImage(mCurrentUser.avatarUrl);
        });
        ivCover.setOnClickListener(v -> {
            if (mCurrentUser != null && mCurrentUser.coverUrl != null) showFullImage(mCurrentUser.coverUrl);
        });
    }

    private void showFullImage(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_view_photo, null);
        builder.setView(view);

        ImageView ivFull = view.findViewById(R.id.iv_full_photo);
        View btnClose = view.findViewById(R.id.btn_close_photo);

        Glide.with(this).load(url).fitCenter().placeholder(R.drawable.placeholder_image).into(ivFull);

        AlertDialog dialog = builder.create();
        btnClose.setOnClickListener(v -> dialog.dismiss());
        ivFull.setOnClickListener(v -> dialog.dismiss());
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
            viewModel.updateProfile(name, bio, dob, avatarUrl != null ? avatarUrl : currentUser.avatarUrl, coverUrl != null ? coverUrl : currentUser.coverUrl);
        });
        bottomSheet.show(getParentFragmentManager(), "EditProfileBottomSheet");
    }

    private void showLoginRequestDialog() {
        RequestLoginDialog dialog = RequestLoginDialog.newInstance();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "RequestLoginDialog");
    }

    @Override public void onLoginClick() { startActivity(new Intent(requireContext(), AuthActivity.class)); }
    @Override public void onCancelClick() {}
}