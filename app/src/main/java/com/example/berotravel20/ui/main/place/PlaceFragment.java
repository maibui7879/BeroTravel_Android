package com.example.berotravel20.ui.main.place;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Favorite.FavoriteResponse;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Review.Review;
import com.example.berotravel20.data.repository.FavoriteRepository;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.data.repository.ReviewRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.main.booking.BookingFragment;
import com.example.berotravel20.ui.map.AddPlaceFragment;
import com.example.berotravel20.ui.map.MapActivity;
import com.example.berotravel20.utils.CategoryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaceFragment extends BaseFragment {
    private String mPlaceId;
    private Place currentPlace;
    private PhotoAdapter photoAdapter;
    private PlaceRepository placeRepository;
    private FavoriteRepository favoriteRepository;
    private ReviewRepository reviewRepository;

    private View layoutContent, layoutNoPhotos;
    private TextView tvTitle, tvLocation, tvDescription, tvPrice, tvPriceLabel, tvCategory, tvRating;
    private Button btnAction, btnDirection;
    private ImageView imgHeader;
    private ImageButton btnFavorite, btnEdit;
    private ImageView[] ratingHearts = new ImageView[5];
    private RecyclerView rvPhotos;

    private boolean isFavorite = false;
    private final List<String> BOOKING_LABEL_CATEGORIES = Arrays.asList(
            "hotel", "motel", "resort", "guest_house", "hostel",
            "restaurant", "bar", "cafe", "fast_food", "pub"
    );

    public static PlaceFragment newInstance(String placeId) {
        PlaceFragment fragment = new PlaceFragment();
        Bundle args = new Bundle();
        args.putString("place_id", placeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mPlaceId = getArguments().getString("place_id");
        placeRepository = new PlaceRepository();
        favoriteRepository = new FavoriteRepository();
        reviewRepository = new ReviewRepository();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (mPlaceId != null) {
            loadPlaceDetails(mPlaceId);
            loadPlaceRating(mPlaceId);
        }
    }

    private void initViews(View view) {
        layoutContent = view.findViewById(R.id.layout_content);
        layoutNoPhotos = view.findViewById(R.id.layout_no_photos);
        tvTitle = view.findViewById(R.id.tv_title);
        tvLocation = view.findViewById(R.id.tv_location);
        tvPriceLabel = view.findViewById(R.id.tv_price_label);
        tvPrice = view.findViewById(R.id.tv_price);
        tvDescription = view.findViewById(R.id.tv_description);
        tvCategory = view.findViewById(R.id.tv_category_label);
        tvRating = view.findViewById(R.id.tv_rating_score);
        btnAction = view.findViewById(R.id.btn_booking);
        btnDirection = view.findViewById(R.id.btn_direction);
        imgHeader = view.findViewById(R.id.img_header);
        btnFavorite = view.findViewById(R.id.btn_favorite);
        btnEdit = view.findViewById(R.id.btn_edit_place);

        ratingHearts[0] = view.findViewById(R.id.heart1);
        ratingHearts[1] = view.findViewById(R.id.heart2);
        ratingHearts[2] = view.findViewById(R.id.heart3);
        ratingHearts[3] = view.findViewById(R.id.heart4);
        ratingHearts[4] = view.findViewById(R.id.heart5);

        rvPhotos = view.findViewById(R.id.rv_photos);
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(pos -> navigateToBooking(1));
        rvPhotos.setAdapter(photoAdapter);

        // Click vào tiêu đề ảnh hoặc nút xem tất cả -> Chuyển sang Photo Tab (index 1)
        View photoHeader = view.findViewById(R.id.layout_photo_header);
        photoHeader.setOnClickListener(v -> navigateToBooking(1));

        btnEdit.setOnClickListener(v -> {
            if (currentPlace != null) {
                replaceFragment(AddPlaceFragment.newInstanceForEdit(
                        currentPlace.id, currentPlace.latitude, currentPlace.longitude));
            }
        });

        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnAction.setOnClickListener(v -> navigateToBooking(2));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBack());

        btnDirection.setOnClickListener(v -> {
            if (currentPlace != null) {
                Intent intent = new Intent(getContext(), MapActivity.class);
                intent.putExtra("ACTION_TYPE", "DIRECT_TO_PLACE");
                intent.putExtra("TARGET_LAT", currentPlace.latitude);
                intent.putExtra("TARGET_LNG", currentPlace.longitude);
                intent.putExtra("TARGET_NAME", currentPlace.name);
                startActivity(intent);
            }
        });
    }

    private void loadPlaceDetails(String id) {
        showLoading();
        placeRepository.getPlaceById(id, new DataCallback<Place>() {
            @Override
            public void onSuccess(Place place) {
                hideLoading();
                layoutContent.setVisibility(View.VISIBLE);
                currentPlace = place;
                displayData(place);
                checkFavoriteStatus();
            }
            @Override public void onError(String msg) { hideLoading(); showError(msg); }
        });
    }

    private void loadPlaceRating(String id) {
        reviewRepository.getPlaceRating(id, new DataCallback<Review.RatingResponse>() {
            @Override
            public void onSuccess(Review.RatingResponse data) {
                if (data != null && isAdded()) {
                    tvRating.setText(data.average <= 0 ? "Chưa có đánh giá" : String.format("%.1f", data.average));
                    updateHeartUI(data.average);
                }
            }
            @Override public void onError(String msg) {
                if (isAdded()) {
                    tvRating.setText("Chưa có đánh giá");
                    updateHeartUI(0);
                }
            }
        });
    }

    private void updateHeartUI(double rating) {
        for (int i = 0; i < 5; i++) {
            if (ratingHearts[i] != null) {
                if (rating >= i + 1) {
                    ratingHearts[i].setImageResource(R.drawable.ic_heart_filled);
                    ratingHearts[i].setColorFilter(Color.parseColor("#FFC107")); // Vàng
                    ratingHearts[i].setAlpha(1.0f);
                } else if (rating > i && rating < i + 1) {
                    ratingHearts[i].setImageResource(R.drawable.ic_heart_filled);
                    ratingHearts[i].setColorFilter(Color.parseColor("#FFC107"));
                    ratingHearts[i].setAlpha(0.5f);
                } else {
                    ratingHearts[i].setImageResource(R.drawable.ic_heart);
                    ratingHearts[i].setColorFilter(Color.parseColor("#D3D3D3")); // Xám
                    ratingHearts[i].setAlpha(1.0f);
                }
            }
        }
    }

    private void displayData(Place place) {
        tvTitle.setText(place.name);
        tvLocation.setText(place.address);
        tvDescription.setText(place.description);
        tvCategory.setText(CategoryUtils.getLabel(place.category));
        Glide.with(this).load(place.imageUrl).placeholder(R.drawable.placeholder_image).into(imgHeader);

        List<String> HOTEL_TYPES = Arrays.asList("hotel", "motel", "resort", "guest_house", "hostel");
        String category = (place.category != null) ? place.category.toLowerCase() : "";

        if (HOTEL_TYPES.contains(category)) {
            tvPriceLabel.setText("Giá phòng");
            String priceStr = place.price == 0 ? "Liên hệ" : String.format("%,.0f đ", place.price);
            tvPrice.setText(priceStr + "/Ngày");
        } else {
            if (category.equals("restaurant") || category.equals("cafe")) {
                tvPriceLabel.setText("Giá trung bình");
            } else {
                tvPriceLabel.setText("Vé vào cửa");
            }
            tvPrice.setText(place.price == 0 ? "Miễn phí" : String.format("%,.0f đ", place.price));
        }

        List<String> images = (place.imgSet != null) ? place.imgSet : new ArrayList<>();
        if (images.isEmpty()) {
            layoutNoPhotos.setVisibility(View.VISIBLE);
            rvPhotos.setVisibility(View.GONE);
        } else {
            layoutNoPhotos.setVisibility(View.GONE);
            rvPhotos.setVisibility(View.VISIBLE);
            photoAdapter.setData(images);
        }

        btnAction.setText(BOOKING_LABEL_CATEGORIES.contains(category) ? "Đặt ngay" : "Thông tin");
    }

    private void toggleFavorite() {
        if (!isUserLoggedIn()) { requireLogin(); return; }
        favoriteRepository.toggleFavorite(mPlaceId, new DataCallback<FavoriteResponse>() {
            @Override public void onSuccess(FavoriteResponse data) {
                isFavorite = !isFavorite;
                btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
                showSuccess(data.message);
            }
            @Override public void onError(String msg) { showError(msg); }
        });
    }

    private void checkFavoriteStatus() {
        if (!isUserLoggedIn() || mPlaceId == null) return;
        favoriteRepository.getMyFavorites(new DataCallback<List<Place>>() {
            @Override public void onSuccess(List<Place> favList) {
                if (favList != null) {
                    for (Place p : favList) if (mPlaceId.equals(p.id)) { isFavorite = true; break; }
                    btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
                }
            }
            @Override public void onError(String msg) {}
        });
    }

    private void navigateToBooking(int tabIndex) {
        if (!isUserLoggedIn()) { requireLogin(); return; }
        if (currentPlace == null) return;
        ArrayList<String> images = new ArrayList<>();
        if (currentPlace.imgSet != null) images.addAll(currentPlace.imgSet);
        replaceFragment(BookingFragment.newInstance(currentPlace.id, currentPlace.name, currentPlace.address,
                currentPlace.imageUrl, (int)currentPlace.price, currentPlace.category, images, tabIndex));
    }
}