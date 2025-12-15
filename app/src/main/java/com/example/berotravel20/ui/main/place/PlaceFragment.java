package com.example.berotravel20.ui.main.place;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.berotravel20.R;
import com.example.berotravel20.network.TokenManager;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaceFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PLACE_ID = "place_id";
    private String mPlaceId;

    public PlaceFragment() {
        // Required empty public constructor
    }

    public static PlaceFragment newInstance(String placeId) {
        PlaceFragment fragment = new PlaceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE_ID, placeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlaceId = getArguments().getString(ARG_PLACE_ID);
        }
    }

    private com.example.berotravel20.network.ApiService apiService;
    private androidx.recyclerview.widget.RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private TextView tvTitle, tvPrice, tvLocation, tvDescription;
    private ImageView imgHeader;
    private TextView tvReadMore;
    private Button btnBooking;
    private boolean isDescriptionExpanded = false;
    private androidx.recyclerview.widget.RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private TextView tvSeeAll;
    private com.example.berotravel20.models.PlaceResponse.Place currentPlace;
    private String currentPlaceId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        android.util.Log.d("PlaceFragment", "onCreateView called");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d("PlaceFragment", "onViewCreated called with PlaceID: " + mPlaceId);

        apiService = com.example.berotravel20.network.ApiClient.getClient(getContext())
                .create(com.example.berotravel20.network.ApiService.class);

        // Init Views
        tvTitle = view.findViewById(R.id.tv_title);
        tvPrice = view.findViewById(R.id.tv_price);
        tvLocation = view.findViewById(R.id.tv_location);
        tvDescription = view.findViewById(R.id.tv_description);
        tvReadMore = view.findViewById(R.id.tv_read_more); // Bind Read More
        imgHeader = view.findViewById(R.id.img_header);
        Button btnAddReview = view.findViewById(R.id.btn_add_review);
        btnBooking = view.findViewById(R.id.btn_booking);
        Button btnDirection = view.findViewById(R.id.btn_direction);

        rvReviews = view.findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(reviewAdapter);

        rvPhotos = view.findViewById(R.id.rv_photos);
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        tvSeeAll = view.findViewById(R.id.tv_see_all_photos);

        tvSeeAll.setOnClickListener(v -> {
            // Logic to be implemented in displayPlace or here if we have data access
            // We need currentPlace data.
            // Ideally we simply check if adapter has data and launch.
        });

        // Toolbar Navigation
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Direction Button Logic
        btnDirection.setOnClickListener(v -> {
            double latitude = 21.0285;
            double longitude = 105.8542;

            com.example.berotravel20.ui.main.map.MapFragment mapFragment = com.example.berotravel20.ui.main.map.MapFragment
                    .newInstance(latitude, longitude);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.base_container, mapFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Add Review Button Logic
        btnAddReview.setOnClickListener(v -> {
            TokenManager tokenManager = new TokenManager(getContext());
            if (tokenManager.getToken() == null) {
                startActivity(
                        new android.content.Intent(getContext(), com.example.berotravel20.ui.auth.LoginActivity.class));
            } else {
                showAddReviewDialog();
            }
        });

        // Booking Button Logic
        btnBooking.setOnClickListener(v -> {
            TokenManager tokenManager = new TokenManager(getContext());
            if (tokenManager.getToken() == null) {
                startActivity(
                        new android.content.Intent(getContext(), com.example.berotravel20.ui.auth.LoginActivity.class));
            } else {
                if (currentPlaceId == null)
                    return;
                int placePrice = 0;
                String priceText = tvPrice.getText().toString();
                if (priceText.contains("$")) {
                    try {
                        String clean = priceText.replaceAll("[^0-9]", "");
                        placePrice = Integer.parseInt(clean);
                    } catch (Exception e) {
                    }
                }

                com.example.berotravel20.ui.main.booking.BookingFragment bookingFragment = com.example.berotravel20.ui.main.booking.BookingFragment
                        .newInstance(currentPlaceId, tvTitle.getText().toString(), placePrice);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.base_container, bookingFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Read More Logic
        tvReadMore.setOnClickListener(v -> {
            if (isDescriptionExpanded) {
                // Collapse
                tvDescription.setMaxLines(3);
                tvDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvReadMore.setText("Read more");
                isDescriptionExpanded = false;
            } else {
                // Expand
                tvDescription.setMaxLines(Integer.MAX_VALUE);
                tvDescription.setEllipsize(null);
                tvReadMore.setText("Read less");
                isDescriptionExpanded = true;
            }
        });

        fetchPlaceData();
    }

    // ... (rest of methods until displayPlace)

    private void showAddReviewDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_review, null);
        builder.setView(dialogView);

        final android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        final android.widget.EditText etComment = dialogView.findViewById(R.id.et_comment);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();
            submitReview(rating, comment);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void submitReview(float rating, String comment) {
        if (currentPlaceId == null)
            return;

        com.example.berotravel20.models.CreateReviewRequest request = new com.example.berotravel20.models.CreateReviewRequest(
                rating, comment);

        apiService.createReview(currentPlaceId, request)
                .enqueue(new retrofit2.Callback<com.example.berotravel20.models.ReviewResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.berotravel20.models.ReviewResponse> call,
                            retrofit2.Response<com.example.berotravel20.models.ReviewResponse> response) {
                        if (response.isSuccessful()) {
                            android.widget.Toast
                                    .makeText(getContext(), "Review submitted!", android.widget.Toast.LENGTH_SHORT)
                                    .show();
                            fetchReviews(currentPlaceId);
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string()
                                        : "Unknown error";
                                android.util.Log.e("PlaceFragment",
                                        "Submit failed: " + response.code() + " - " + errorBody);
                                android.widget.Toast
                                        .makeText(getContext(), "Failed: " + response.code() + " " + errorBody,
                                                android.widget.Toast.LENGTH_LONG)
                                        .show();
                            } catch (java.io.IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.berotravel20.models.ReviewResponse> call,
                            Throwable t) {
                        android.widget.Toast
                                .makeText(getContext(), "Error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void fetchPlaceData() {
        if (mPlaceId != null) {
            apiService.getPlaces(50, null)
                    .enqueue(new retrofit2.Callback<com.example.berotravel20.models.PlaceResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                                retrofit2.Response<com.example.berotravel20.models.PlaceResponse> response) {
                            android.util.Log.d("PlaceFragment", "Fetch response: " + response.code());
                            if (response.isSuccessful() && response.body() != null && !response.body().data.isEmpty()) {
                                // Find the one matching ID
                                com.example.berotravel20.models.PlaceResponse.Place found = null;
                                for (com.example.berotravel20.models.PlaceResponse.Place p : response.body().data) {
                                    if (p._id.equals(mPlaceId)) {
                                        found = p;
                                        break;
                                    }
                                }

                                // Fallback if not found (pagination?) or default logic
                                if (found == null) {
                                    android.util.Log.d("PlaceFragment",
                                            "Place ID not found in list, defaulting to first");
                                    found = response.body().data.get(0);
                                }

                                android.util.Log.d("PlaceFragment", "Displaying place: " + found.name);
                                android.util.Log.d("PlaceFragment", "Displaying place: " + found.name);
                                currentPlaceId = found._id;
                                currentPlace = found;
                                displayPlace(found);
                                fetchReviews(found._id);
                            } else {
                                android.util.Log.e("PlaceFragment", "Fetch failed or empty data");
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                                Throwable t) {
                            android.util.Log.e("PlaceFragment", "Error fetching places", t);
                        }
                    });

        } else {
            apiService.getPlaces(50, null)
                    .enqueue(new retrofit2.Callback<com.example.berotravel20.models.PlaceResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                                retrofit2.Response<com.example.berotravel20.models.PlaceResponse> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().data.isEmpty()) {
                                com.example.berotravel20.models.PlaceResponse.Place place = response.body().data.get(0);

                                currentPlaceId = place._id;
                                currentPlace = place;
                                displayPlace(place);
                                fetchReviews(place._id);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                                Throwable t) {
                            android.util.Log.e("PlaceFragment", "Error fetching places", t);
                        }
                    });
        }
    }

    private void displayPlace(com.example.berotravel20.models.PlaceResponse.Place place) {
        tvTitle.setText(place.name);
        tvLocation.setText(place.address);
        tvDescription.setText(place.description);

        // Handle Description Expansion Logic
        tvDescription.post(() -> {
            if (tvDescription.getLineCount() > 3) {
                tvReadMore.setVisibility(View.VISIBLE);
                tvDescription.setMaxLines(3);
                tvDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
                isDescriptionExpanded = false;
            } else {
                tvReadMore.setVisibility(View.GONE);
            }
        });

        double priceValue = place.price;
        if (place.status != null && place.status.price > 0) {
            priceValue = place.status.price;
        }

        if ("Hotel".equalsIgnoreCase(place.category) || "Homestay".equalsIgnoreCase(place.category)) {
            // Hotel: Show Booking, Price/Day
            if (btnBooking != null)
                btnBooking.setVisibility(View.VISIBLE);
            tvPrice.setText(String.format("%,.0f/ngày", priceValue));
            tvPrice.setTextColor(android.graphics.Color.BLACK);
        } else {
            // Place: Hide Booking, Ticket Price
            if (btnBooking != null)
                btnBooking.setVisibility(View.GONE);
            if (priceValue == 0) {
                tvPrice.setText("Vé vào cửa: Miễn phí");
                tvPrice.setTextColor(android.graphics.Color.GREEN);
            } else {
                tvPrice.setText(String.format("Vé vào cửa: %,.0f", priceValue));
                tvPrice.setTextColor(android.graphics.Color.BLACK);
            }
        }

        // Image Logic with Debugging
        if (place.image_url != null && !place.image_url.isEmpty()) {
            android.util.Log.d("PlaceFragment", "Loading image: " + place.image_url);
            com.bumptech.glide.Glide.with(this)
                    .load(place.image_url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                                Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                boolean isFirstResource) {
                            android.util.Log.e("PlaceFragment", "Glide load failed", e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            android.util.Log.d("PlaceFragment", "Glide load success");
                            return false;
                        }
                    })
                    .into(imgHeader);
        } else {
            android.util.Log.d("PlaceFragment", "Image URL is null or empty");
        }

        // Photo Gallery Logic
        java.util.ArrayList<String> photos = new java.util.ArrayList<>();
        if (place.img_set != null && !place.img_set.isEmpty()) {
            photos.addAll(place.img_set);
        } else if (place.image_url != null) {
            photos.add(place.image_url);
        }

        if (photos.isEmpty()) {
            rvPhotos.setVisibility(View.GONE);
            tvSeeAll.setVisibility(View.GONE);
        } else {
            rvPhotos.setVisibility(View.VISIBLE);
            tvSeeAll.setVisibility(View.VISIBLE);

            photoAdapter = new PhotoAdapter(photos, position -> {
                openGallery(photos, position);
            });
            rvPhotos.setAdapter(photoAdapter);

            tvSeeAll.setOnClickListener(v -> openGallery(photos, 0));
        }
    }

    private void openGallery(java.util.ArrayList<String> photos, int startPos) {
        PhotoGalleryFragment fragment = PhotoGalleryFragment.newInstance(photos, startPos);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.base_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchReviews(String placeId) {
        apiService.getReviews(placeId)
                .enqueue(new retrofit2.Callback<java.util.List<com.example.berotravel20.models.ReviewResponse>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<java.util.List<com.example.berotravel20.models.ReviewResponse>> call,
                            retrofit2.Response<java.util.List<com.example.berotravel20.models.ReviewResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            reviewAdapter = new ReviewAdapter(response.body(), (review, voteType) -> {
                                handleVote(review, voteType);
                            });
                            rvReviews.setAdapter(reviewAdapter);
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<java.util.List<com.example.berotravel20.models.ReviewResponse>> call,
                            Throwable t) {
                        android.util.Log.e("PlaceFragment", "Error fetching reviews", t);
                    }
                });
    }

    private void handleVote(com.example.berotravel20.models.ReviewResponse review, String voteType) {
        String token = new TokenManager(getContext()).getToken();
        if (token == null) {
            startActivity(
                    new android.content.Intent(getContext(), com.example.berotravel20.ui.auth.LoginActivity.class));
            return;
        }

        // Optimistic UI Update can be tricky without deep copy, but let's try or just
        // fetch again.
        // For simplicity and correctness, let's call API and then re-fetch reviews or
        // update distinct item.
        // Re-fetching is safer for now.

        com.example.berotravel20.models.VoteRequest request = new com.example.berotravel20.models.VoteRequest(voteType);
        apiService.voteReview(review._id, request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    // Refresh reviews to get updated scores
                    if (currentPlace != null) {
                        fetchReviews(currentPlace._id);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "Unknown error";
                        android.util.Log.e("PlaceFragment", "Vote failed: " + response.code() + " - " + errorBody);
                        android.widget.Toast.makeText(getContext(), "Vote failed: " + response.code(),
                                android.widget.Toast.LENGTH_SHORT).show();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                android.util.Log.e("PlaceFragment", "Vote error", t);
                android.widget.Toast
                        .makeText(getContext(), "Error voting: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}