package com.example.berotravel20.ui.main.place;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.berotravel20.data.repository.FavoriteRepository;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.main.booking.BookingFragment;
import com.example.berotravel20.ui.map.MapActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaceFragment extends BaseFragment {
    private String mPlaceId;
    private Place currentPlace;
    private PhotoAdapter photoAdapter;
    private PlaceRepository placeRepository;
    private FavoriteRepository favoriteRepository;

    // UI Components
    private ProgressBar pbLoading;
    private View layoutContent;
    private TextView tvTitle, tvLocation, tvDescription, tvPrice;
    private Button btnAction, btnDirection;
    private ImageView imgHeader;
    private ImageButton btnFavorite;

    // State
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (mPlaceId != null) loadPlaceDetails(mPlaceId);
    }

    private void initViews(View view) {
        pbLoading = view.findViewById(R.id.pb_loading);
        layoutContent = view.findViewById(R.id.layout_content);
        tvTitle = view.findViewById(R.id.tv_title);
        tvLocation = view.findViewById(R.id.tv_location);
        tvPrice = view.findViewById(R.id.tv_price);
        tvDescription = view.findViewById(R.id.tv_description);
        btnAction = view.findViewById(R.id.btn_booking);
        btnDirection = view.findViewById(R.id.btn_direction);
        imgHeader = view.findViewById(R.id.img_header);
        btnFavorite = view.findViewById(R.id.btn_favorite); // ĐÃ KHÔI PHỤC

        RecyclerView rvPhotos = view.findViewById(R.id.rv_photos);
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(pos -> navigateToBooking(1));
        rvPhotos.setAdapter(photoAdapter);

        // Events
        view.findViewById(R.id.tv_see_all_photos).setOnClickListener(v -> navigateToBooking(1));
        btnAction.setOnClickListener(v -> navigateToBooking(2));
        btnFavorite.setOnClickListener(v -> toggleFavorite()); // ĐÃ KHÔI PHỤC

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

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
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        placeRepository.getPlaceById(id, new DataCallback<Place>() {
            @Override
            public void onSuccess(Place place) {
                if (!isAdded() || getView() == null) return;
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                currentPlace = place;
                displayData(place);
                checkFavoriteStatus(); // ĐÃ KHÔI PHỤC
            }
            @Override
            public void onError(String msg) {
                if (!isAdded()) return;
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                showError(msg);
            }
        });
    }

    private void displayData(Place place) {
        tvTitle.setText(place.name);
        tvLocation.setText(place.address);
        tvDescription.setText(place.description);
        tvPrice.setText(place.price == 0 ? "Miễn phí" : String.format("%,.0f đ", place.price));

        if (place.category != null && BOOKING_LABEL_CATEGORIES.contains(place.category.toLowerCase().trim())) {
            btnAction.setText("Đặt ngay");
        } else {
            btnAction.setText("Xem chi tiết");
        }

        Glide.with(this).load(place.imageUrl).placeholder(R.drawable.placeholder_image).into(imgHeader);
        List<String> images = new ArrayList<>();
        if (place.imgSet != null) images.addAll(place.imgSet);
        photoAdapter.setData(images);
    }

    // --- LOGIC FAVORITE MỚI ---
    private void toggleFavorite() {
        if (!isUserLoggedIn()) { requireLogin(); return; }
        if (mPlaceId == null) return;

        favoriteRepository.toggleFavorite(mPlaceId, new DataCallback<FavoriteResponse>() {
            @Override
            public void onSuccess(FavoriteResponse data) {
                isFavorite = !isFavorite;
                updateFavoriteIcon();
                showSuccess(data.message);
            }
            @Override public void onError(String msg) { showError(msg); }
        });
    }

    private void checkFavoriteStatus() {
        if (!isUserLoggedIn() || mPlaceId == null) return;
        favoriteRepository.getMyFavorites(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> favList) {
                if (favList != null) {
                    isFavorite = false;
                    for (Place p : favList) {
                        if (mPlaceId.equals(p.id)) { isFavorite = true; break; }
                    }
                    updateFavoriteIcon();
                }
            }
            @Override public void onError(String msg) {}
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
    }

    // --- LOGIC NAVIGATE ---
    private void navigateToBooking(int tabIndex) {
        if (!isUserLoggedIn()) { requireLogin(); return; }
        if (currentPlace == null) return;
        ArrayList<String> images = new ArrayList<>();
        if (currentPlace.imgSet != null) images.addAll(currentPlace.imgSet);

        BookingFragment fragment = BookingFragment.newInstance(
                currentPlace.id, currentPlace.name, currentPlace.address,
                currentPlace.imageUrl, (int)currentPlace.price,currentPlace.category, images, tabIndex);
        replaceFragment(fragment);
    }
}