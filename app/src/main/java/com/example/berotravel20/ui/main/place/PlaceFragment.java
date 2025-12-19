package com.example.berotravel20.ui.main.place;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.berotravel20.R;
import com.example.berotravel20.data.local.TokenManager;
import android.widget.Button;
import android.widget.Spinner;
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
        imgHeader = view.findViewById(R.id.img_header);
        rvPhotos = view.findViewById(R.id.rv_photos);
        rvReviews = view.findViewById(R.id.rv_reviews);
        tvSeeAll = view.findViewById(R.id.tv_see_all_photos);
        // ratingBar = view.findViewById(R.id.rating_bar_display); // This line was not
        // in the original code, adding it would require a new import and declaration.
        // Keeping original behavior.
        tvReadMore = view.findViewById(R.id.tv_read_more);
        Button btnDirection = view.findViewById(R.id.btn_direction); // Declared here as it's used only in this method
        Button btnAddReview = view.findViewById(R.id.btn_add_review); // Declared here as it's used only in this method
        btnBooking = view.findViewById(R.id.btn_booking);

        // RecyclerView Setup
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        tvSeeAll.setOnClickListener(v -> {
            // Logic to be implemented in displayPlace or here if we have data access
        });

        // Toolbar Navigation
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Direction Button Logic
        btnDirection.setOnClickListener(v -> {
            double latitude = 21.0285; // Default or fetched
            double longitude = 105.8542;

            if (currentPlace != null) {
                longitude = currentPlace.longitude;
                latitude = currentPlace.latitude;
            }

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
                return;
            }

            if (currentPlaceId == null)
                return;

            int placePrice = 0;
            String priceText = tvPrice.getText().toString();
            try {
                // Simple parse if possible, or use Place object
                if (currentPlace != null)
                    placePrice = (int) currentPlace.price;
            } catch (Exception e) {
            }

            String address = "";
            String imageUrl = "";
            if (currentPlace != null) {
                address = currentPlace.address;
                imageUrl = currentPlace.image_url;
            } else {
                address = tvLocation.getText().toString();
            }

            com.example.berotravel20.ui.main.booking.BookingFragment bookingFragment = com.example.berotravel20.ui.main.booking.BookingFragment
                    .newInstance(currentPlaceId, tvTitle.getText().toString(), address, imageUrl, placePrice);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.base_container, bookingFragment)
                    .addToBackStack(null)
                    .commit();
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

        Button btnAddSchedule = view.findViewById(R.id.btn_add_to_schedule);
        btnAddSchedule.setOnClickListener(v -> checkScheduleAndAdd());

        fetchPlaceData();
    }

    private void checkScheduleAndAdd() {
        if (currentPlace == null)
            return;

        com.example.berotravel20.data.local.TokenManager tokenManager = new com.example.berotravel20.data.local.TokenManager(
                getContext());
        if (tokenManager.getToken() == null) {
            startActivity(
                    new android.content.Intent(getContext(), com.example.berotravel20.ui.auth.LoginActivity.class));
            return;
        }

        // Fetch user journeys to populate dialog
        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getJourneyApi()
                .getJourneys()
                .enqueue(new retrofit2.Callback<com.example.berotravel20.models.JourneyResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.berotravel20.models.JourneyResponse> call,
                            retrofit2.Response<com.example.berotravel20.models.JourneyResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            showJourneySelectionDialog(response.body().data);
                        } else {
                            showJourneySelectionDialog(new java.util.ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<com.example.berotravel20.models.JourneyResponse> call,
                            Throwable t) {
                        showJourneySelectionDialog(new java.util.ArrayList<>());
                    }
                });
    }

    private void showJourneySelectionDialog(
            java.util.List<com.example.berotravel20.data.model.Journey.Journey> journeys) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Chọn chuyến đi");

        // Filter ongoing/upcoming
        java.util.List<com.example.berotravel20.data.model.Journey.Journey> validJourneys = new java.util.ArrayList<>();
        java.util.List<String> options = new java.util.ArrayList<>();

        for (com.example.berotravel20.data.model.Journey.Journey j : journeys) {
            if ("ongoing".equals(j.status)) { // or check dates
                validJourneys.add(j);
                options.add(j.location + " (" + formatDate(j.startDate) + " - " + formatDate(j.endDate) + ")");
            }
        }
        options.add("+ Tạo chuyến đi mới");

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, options);

        builder.setAdapter(adapter, (dialog, which) -> {
            if (which == options.size() - 1) {
                // Last item: Create New
                showCreateTripDialogWithDates();
            } else {
                // Existing Journey
                com.example.berotravel20.data.model.Journey.Journey selected = validJourneys.get(which);
                // Validate Location
                if (validateLocationMatch(selected.location)) {
                    addPlaceToExistingJourney(selected.id, selected.location);
                } else {
                    showMismatchDialog(selected.location, getPlaceLocation());
                }
            }
        });
        builder.show();
    }

    private boolean validateLocationMatch(String tripLocation) {
        if (currentPlace == null)
            return false;
        String placeAddr = currentPlace.address != null ? currentPlace.address : "";
        if (tripLocation.equals("Hà Nội") && placeAddr.contains("Hà Nội"))
            return true;
        if (tripLocation.equals("TP.HCM") && (placeAddr.contains("Hồ Chí Minh") || placeAddr.contains("TP.HCM")))
            return true;
        if (tripLocation.equals("Đà Nẵng") && placeAddr.contains("Đà Nẵng"))
            return true;
        return false;
    }

    private String getPlaceLocation() {
        String address = currentPlace.address != null ? currentPlace.address : "";
        if (address.contains("Hà Nội"))
            return "Hà Nội";
        if (address.contains("Hồ Chí Minh") || address.contains("TP.HCM"))
            return "TP.HCM";
        if (address.contains("Đà Nẵng"))
            return "Đà Nẵng";
        return "Unknown";
    }

    private void showCreateTripDialogWithDates() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_trip, null);
        builder.setView(view);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        Spinner spinner = view.findViewById(R.id.spinner_location);
        android.widget.EditText etTripName = view.findViewById(R.id.et_trip_name);
        TextView tvStart = view.findViewById(R.id.tv_start_date);
        TextView tvEnd = view.findViewById(R.id.tv_end_date);
        View btnCancel = view.findViewById(R.id.btn_cancel);
        View btnCreate = view.findViewById(R.id.btn_create);

        java.util.List<String> locations = new java.util.ArrayList<>();
        locations.add("Hà Nội");
        locations.add("TP.HCM");
        locations.add("Đà Nẵng");
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Fetch destinations dynamic
        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getPlaceApi()
                .getDestinations()
                .enqueue(new retrofit2.Callback<com.example.berotravel20.models.DestinationResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.berotravel20.models.DestinationResponse> call,
                            retrofit2.Response<com.example.berotravel20.models.DestinationResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            if (response.body().data != null && !response.body().data.isEmpty()) {
                                locations.clear();
                                locations.addAll(response.body().data);
                                adapter.notifyDataSetChanged();

                                // Retry selection after data load
                                try {
                                    String placeLoc = getPlaceLocation();
                                    if (!"Unknown".equals(placeLoc)) {
                                        int pos = locations.indexOf(placeLoc);
                                        if (pos >= 0)
                                            spinner.setSelection(pos);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.berotravel20.models.DestinationResponse> call,
                            Throwable t) {
                    }
                });

        // Auto-select based on place logic
        String placeLoc = getPlaceLocation();
        if (!"Unknown".equals(placeLoc)) {
            int pos = locations.indexOf(placeLoc);
            if (pos >= 0)
                spinner.setSelection(pos);
        }

        tvStart.setOnClickListener(v -> showDatePicker(tvStart));
        tvEnd.setOnClickListener(v -> showDatePicker(tvEnd));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String selectedLoc = spinner.getSelectedItem().toString();
            String start = tvStart.getText().toString();
            String end = tvEnd.getText().toString();
            String tripName = etTripName.getText().toString();

            if (start.isEmpty() || end.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Vui lòng chọn ngày", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (!validateLocationMatch(selectedLoc)) {
                android.widget.Toast.makeText(getContext(), "Địa điểm không khớp với nơi đã chọn",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            createJourneyAndAddPlace(selectedLoc, start, end, tripName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePicker(TextView view) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(getContext(), (picker, y, m, d) -> {
            view.setText(d + "/" + (m + 1) + "/" + y);
        }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH))
                .show();
    }

    // Helper to format date for display
    private String formatDate(String dateStr) {
        if (dateStr == null)
            return "";
        try {
            if (dateStr.contains("T"))
                return dateStr.split("T")[0]; // Simple YYYY-MM-DD
        } catch (Exception e) {
        }
        return dateStr;
    }

    private void createJourneyAndAddPlace(String location, String start, String end, String name) {
        com.example.berotravel20.models.AddPlaceRequest request = new com.example.berotravel20.models.AddPlaceRequest(
                currentPlace._id, location, convertDate(start), convertDate(end), name, true);

        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getJourneyApi()
                .addPlaceToJourney(request)
                .enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call,
                            retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (response.isSuccessful()) {
                            android.widget.Toast.makeText(getContext(), "Đã tạo chuyến đi và thêm địa điểm!",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String error = response.errorBody().string();
                                org.json.JSONObject json = new org.json.JSONObject(error);
                                android.widget.Toast
                                        .makeText(getContext(), json.optString("message", "Lỗi tạo chuyến đi"),
                                                android.widget.Toast.LENGTH_LONG)
                                        .show();
                            } catch (Exception e) {
                                android.widget.Toast.makeText(getContext(), "Lỗi tạo chuyến đi: " + response.code(),
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                        android.widget.Toast.makeText(getContext(), "Lỗi mạng", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private String convertDate(String date) {
        try {
            String[] parts = date.split("/");
            if (parts.length == 3)
                return parts[2] + "-" + parts[1] + "-" + parts[0];
        } catch (Exception e) {
        }
        return date;
    }

    private void addPlaceToExistingJourney(String journeyId, String journeyLocation) {
        showAddTimeDialog(currentPlace._id, journeyLocation, journeyId);
    }

    private void savePlaceToSchedule(android.content.SharedPreferences prefs) {
        // Legacy support
    }

    private void showAddTimeDialog(String placeId, String location, String journeyId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_place_time, null);
        builder.setView(view);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tvStartTime = view.findViewById(R.id.tv_start_time_display);
        TextView tvEndTime = view.findViewById(R.id.tv_end_time_display);
        View btnCancel = view.findViewById(R.id.btn_cancel);
        View btnCreate = view.findViewById(R.id.btn_create);
        View containerStart = view.findViewById(R.id.container_start_time);
        View containerEnd = view.findViewById(R.id.container_end_time);

        containerStart.setOnClickListener(v -> showDateTimePicker(tvStartTime));
        containerEnd.setOnClickListener(v -> showDateTimePicker(tvEndTime));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String startTime = tvStartTime.getText().toString();
            String endTime = tvEndTime.getText().toString();

            if (startTime.isEmpty() || endTime.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Vui lòng chọn thời gian bắt đầu và kết thúc",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            syncPlaceToServer(placeId, location, journeyId, convertToISO(startTime), convertToISO(endTime));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDateTimePicker(TextView textView) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(getContext(), (picker, y, m, d) -> {
            new android.app.TimePickerDialog(getContext(), (timePicker, hour, minute) -> {
                String formatted = String.format(java.util.Locale.getDefault(), "%02d/%02d/%d %02d:%02d", d, m + 1, y,
                        hour, minute);
                textView.setText(formatted);
            }, c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE), true).show();
        }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH))
                .show();
    }

    // Convert DD/MM/YYYY HH:mm to ISO 8601 for backend
    private String convertToISO(String dateTime) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(dateTime);
            java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    java.util.Locale.getDefault());
            iso.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return iso.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateTime; // fallback
        }
    }

    private void syncPlaceToServer(String placeId, String location, String journeyId, String startTime,
            String endTime) {
        com.example.berotravel20.models.AddPlaceRequest request = new com.example.berotravel20.models.AddPlaceRequest(
                placeId, location, journeyId, startTime, endTime);

        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getJourneyApi()
                .addPlaceToJourney(request)
                .enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call,
                            retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (response.isSuccessful()) {
                            android.widget.Toast
                                    .makeText(getContext(), "Đã thêm vào chuyến đi!", android.widget.Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            try {
                                String error = response.errorBody().string();
                                org.json.JSONObject json = new org.json.JSONObject(error);
                                android.widget.Toast
                                        .makeText(getContext(), json.optString("message", "Lỗi: " + response.code()),
                                                android.widget.Toast.LENGTH_LONG)
                                        .show();
                            } catch (Exception e) {
                                android.widget.Toast.makeText(getContext(), "Lỗi thêm địa điểm: " + response.code(),
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                        android.widget.Toast.makeText(getContext(), "Lỗi mạng", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void syncPlaceToServer(String placeId, String location) {
        com.example.berotravel20.models.AddPlaceRequest request = new com.example.berotravel20.models.AddPlaceRequest(
                placeId, location);

        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getJourneyApi()
                .addPlaceToJourney(request)
                .enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call,
                            retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (response.isSuccessful()) {
                            android.util.Log.d("PlaceFragment", "Immediate sync success");
                        } else {
                            android.util.Log.e("PlaceFragment", "Immediate sync error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                        android.util.Log.e("PlaceFragment", "Immediate sync failure: " + t.getMessage());
                    }
                });
    }

    private void showMismatchDialog(String current, String newLoc) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Lịch trình không khớp")
                .setMessage("Địa điểm này không thuộc chuyến đi " + current + ".\nBạn có muốn tạo chuyến đi mới đến "
                        + newLoc + " không?")
                .setPositiveButton("Tạo chuyến đi mới", (dialog, which) -> {
                    saveTripLocation(newLoc);
                    // Clear old places when starting new trip
                    requireContext().getSharedPreferences("BeroTravelPrefs", android.content.Context.MODE_PRIVATE)
                            .edit().putString("CURRENT_TRIP_PLACES", "[]").apply();

                    android.widget.Toast
                            .makeText(getContext(), "Đã chuyển sang chuyến đi " + newLoc + ". Đang thêm địa điểm...",
                                    android.widget.Toast.LENGTH_SHORT)
                            .show();
                    // Add the current place
                    savePlaceToSchedule(requireContext().getSharedPreferences("BeroTravelPrefs",
                            android.content.Context.MODE_PRIVATE));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveTripLocation(String location) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("BeroTravelPrefs",
                android.content.Context.MODE_PRIVATE);
        prefs.edit().putString("CURRENT_TRIP_LOCATION", location).apply();
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

        tvReadMore.setOnClickListener(v -> {
            if (isDescriptionExpanded) {
                tvDescription.setMaxLines(3);
                tvDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvReadMore.setText(getString(R.string.read_more));
                isDescriptionExpanded = false;
            } else {
                tvDescription.setMaxLines(Integer.MAX_VALUE);
                tvDescription.setEllipsize(null);
                tvReadMore.setText(getString(R.string.read_less));
                isDescriptionExpanded = true;
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
                tvPrice.setText(getString(R.string.free_ticket));
                tvPrice.setTextColor(android.graphics.Color.GREEN);
            } else {
                tvPrice.setText(getString(R.string.ticket_price_format, priceValue));
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