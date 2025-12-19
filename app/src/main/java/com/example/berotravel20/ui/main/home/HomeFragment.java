package com.example.berotravel20.ui.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.api.WeatherApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceResponse;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.model.Weather.WeatherResponse;
import com.example.berotravel20.data.remote.RetrofitClient;
import com.example.berotravel20.data.repository.PlaceRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.main.notification.NotificationFragment;
import com.example.berotravel20.ui.map.LocationHelper;
import com.example.berotravel20.ui.map.MapActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends BaseFragment {

    // --- UI Components ---
    private TextView tvUsername, tvTemperature, tvWeatherDesc, tvHumidity, tvWindSpeed, tvCityName;
    private TextView tvSuggestedTitle;
    private ImageView ivAvatar, ivWeatherIcon;
    private EditText etSearch; // Thêm EditText
    private View loadingLayout, mainContent;

    // --- Logic & Data ---
    private PlaceRepository placeRepository;
    private LocationHelper locationHelper;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private boolean isLocationFound = false;

    // --- Adapters ---
    private MapPlaceAdapter suggestAdapter;
    private MapPlaceAdapter hotelAdapter;
    private MapPlaceAdapter restaurantAdapter;

    // --- Constants ---
    private static final String WEATHER_API_KEY = "144fd485cfd342f1282c4b1e82446243";
    private static final String BASE_URL_WEATHER = "https://api.openweathermap.org/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeRepository = new PlaceRepository();
        locationHelper = new LocationHelper(getActivity());

        initViews(view);
        setupAdapters(view);
        setupCategoryEvents(view);
        loadUserProfile();
        if (!isLocationFound) {
            setupLocationLogic();
        } else {
            // Nếu đã có dữ liệu (khi Back về), ẩn loading ngay lập tức
            if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
            if (mainContent != null) mainContent.setVisibility(View.VISIBLE);
        }

    }

    private void initViews(View view) {
        loadingLayout = view.findViewById(R.id.loadingLayout);
        mainContent = view.findViewById(R.id.mainContent);

        tvUsername = view.findViewById(R.id.tvUsername);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed);
        tvCityName = view.findViewById(R.id.tvCityName);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        etSearch = view.findViewById(R.id.etSearch);

        // --- [QUAN TRỌNG] XỬ LÝ SEARCH INPUT ---
        if (etSearch != null) {
            // 1. Xử lý khi bấm nút Search trên bàn phím ảo
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearchLogic();
                    return true;
                }
                return false;
            });

            // 2. Xử lý khi bấm trực tiếp vào icon Search (Drawable Right/End)
            etSearch.setOnTouchListener((v, event) -> {
                final int DRAWABLE_RIGHT = 2; // Index của drawable bên phải

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Kiểm tra drawable có tồn tại không
                    if (etSearch.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                        // Tính toán vùng chạm: Nếu x >= (Chiều rộng view - chiều rộng icon - padding) -> Đã chạm icon
                        if (event.getRawX() >= (etSearch.getRight() - etSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - etSearch.getPaddingEnd() - 30)) {
                            performSearchLogic();
                            return true; // Đã xử lý, không truyền sự kiện đi tiếp
                        }
                    }
                }
                return false; // Trả về false để các sự kiện khác (như focus) vẫn hoạt động
            });
        }

        // Ánh xạ an toàn cho Title
        View secSuggest = view.findViewById(R.id.secSuggested);
        if (secSuggest != null) {
            tvSuggestedTitle = secSuggest.findViewById(R.id.tvSectionTitle);
            if (tvSuggestedTitle != null) {
                tvSuggestedTitle.setText("Điểm tham quan gợi ý");
            }
        }

        view.findViewById(R.id.btnNotification).setOnClickListener(v ->
                replaceFragment(NotificationFragment.newInstance())
        );
    }

    // Hàm logic chung cho cả 2 sự kiện search
    private void performSearchLogic() {
        String query = etSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            navigateToMapSearch(query);
        } else {
            Toast.makeText(getContext(), "Vui lòng nhập từ khóa", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMapSearch(String query) {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra("SEARCH_QUERY", query);
        startActivity(intent);
    }

    private void setupAdapters(View view) {
        MapPlaceAdapter.OnItemClickListener listener = createListener();

        // 1. Section Gợi ý: Dùng ITEM DỌC (item_place_vertical)
        View secSuggest = view.findViewById(R.id.secSuggested);
        if (secSuggest != null) {
            RecyclerView rvSuggest = secSuggest.findViewById(R.id.rv_home_list);
            rvSuggest.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            // Dùng Constructor tùy chỉnh layout
            suggestAdapter = new MapPlaceAdapter(getContext(), R.layout.item_place_vertical, listener);
            rvSuggest.setAdapter(suggestAdapter);
        }

        // 2. Section Khách sạn: Dùng Item Ngang Mặc Định
        setupSection(view.findViewById(R.id.secHotels), "Khách sạn gần bạn",
                LinearLayoutManager.VERTICAL, hotelAdapter = new MapPlaceAdapter(getContext(), listener));

        // 3. Section Nhà hàng: Dùng Item Ngang Mặc Định
        setupSection(view.findViewById(R.id.secRestaurants), "Nhà hàng ngon gần bạn",
                LinearLayoutManager.HORIZONTAL, restaurantAdapter = new MapPlaceAdapter(getContext(), listener));
    }

    private void setupSection(View secView, String title, int orientation, MapPlaceAdapter adapter) {
        if (secView == null) return;
        TextView tvTitle = secView.findViewById(R.id.tvSectionTitle);
        if (tvTitle != null) tvTitle.setText(title);

        RecyclerView rv = secView.findViewById(R.id.rv_home_list);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext(), orientation, false));
            rv.setAdapter(adapter);
        }
    }

    private void setupCategoryEvents(View view) {
        View.OnClickListener catListener = v -> {
            if (!isLocationFound) {
                Toast.makeText(getContext(), "Đang xác định vị trí...", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = null;
            String title = "Gợi ý";

            int id = v.getId();
            if (id == R.id.btnCatPark) { category = "park"; title = "Công viên xanh"; }
            else if (id == R.id.btnCatRestaurant) { category = "restaurant"; title = "Nhà hàng quanh đây"; }
            else if (id == R.id.btnCatAttraction) { category = "tourist_attraction"; title = "Điểm tham quan"; }
            else if (id == R.id.btnCatHotel) { category = "lodging"; title = "Nơi lưu trú"; }
            else if (id == R.id.btnCatBar) { category = "bar"; title = "Quán bar & Pub"; }

            if (tvSuggestedTitle != null) {
                tvSuggestedTitle.setText(title);
            }

            filterSuggestionByCategory(category);
        };

        View btnPark = view.findViewById(R.id.btnCatPark);
        View btnRest = view.findViewById(R.id.btnCatRestaurant);
        View btnAttr = view.findViewById(R.id.btnCatAttraction);
        View btnHotel = view.findViewById(R.id.btnCatHotel);
        View btnBar = view.findViewById(R.id.btnCatBar);

        if (btnPark != null) btnPark.setOnClickListener(catListener);
        if (btnRest != null) btnRest.setOnClickListener(catListener);
        if (btnAttr != null) btnAttr.setOnClickListener(catListener);
        if (btnHotel != null) btnHotel.setOnClickListener(catListener);
        if (btnBar != null) btnBar.setOnClickListener(catListener);
    }

    private void setupLocationLogic() {
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);
        if (mainContent != null) mainContent.setVisibility(View.INVISIBLE);

        if (locationHelper.hasPermission()) {
            locationHelper.getLastLocation(location -> {
                if (location != null && isAdded()) {
                    handleLocationFound(location);
                } else {
                    locationHelper.startLocationUpdates(loc -> {
                        if (loc != null && isAdded()) {
                            handleLocationFound(loc);
                            locationHelper.stopLocationUpdates();
                        }
                    });
                }
            });
        } else {
            locationHelper.requestPermission();
            if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
            if (tvCityName != null) tvCityName.setText("Cần quyền vị trí");
        }
    }

    private void handleLocationFound(android.location.Location location) {
        if (isLocationFound) return;

        currentLat = location.getLatitude();
        currentLng = location.getLongitude();
        isLocationFound = true;

        if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
        if (mainContent != null) mainContent.setVisibility(View.VISIBLE);

        fetchWeather(currentLat, currentLng);
        loadInitialData(currentLat, currentLng);
    }

    private void loadInitialData(double lat, double lng) {
        int radius = 5000;
        filterSuggestionByCategory(null);

        placeRepository.searchNearby(lat, lng, radius, null, "hotel", 1, 10, new DataCallback<PlaceResponse>() {
            @Override public void onSuccess(PlaceResponse d) { if (isAdded()) hotelAdapter.setData(d.data); }
            @Override public void onError(String msg) { }
        });

        placeRepository.searchNearby(lat, lng, radius, null, "restaurant", 1, 10, new DataCallback<PlaceResponse>() {
            @Override public void onSuccess(PlaceResponse d) { if (isAdded()) restaurantAdapter.setData(d.data); }
            @Override public void onError(String msg) { }
        });
    }

    private void filterSuggestionByCategory(String category) {
        if (suggestAdapter != null) suggestAdapter.clearData();
        placeRepository.searchNearby(currentLat, currentLng, 5000, null, category, 1, 10, new DataCallback<PlaceResponse>() {
            @Override public void onSuccess(PlaceResponse data) { if (isAdded() && data != null) suggestAdapter.setData(data.data); }
            @Override public void onError(String msg) { }
        });
    }

    private MapPlaceAdapter.OnItemClickListener createListener() {
        return new MapPlaceAdapter.OnItemClickListener() {
            @Override public void onFavoriteClick(Place p) {
                if (suggestAdapter != null) suggestAdapter.toggleFavoriteLocal(p.id);
                if (hotelAdapter != null) hotelAdapter.toggleFavoriteLocal(p.id);
                if (restaurantAdapter != null) restaurantAdapter.toggleFavoriteLocal(p.id);
                Toast.makeText(getContext(), "Đã cập nhật yêu thích", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void fetchWeather(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService service = retrofit.create(WeatherApiService.class);
        service.getCurrentWeather(lat, lon, WEATHER_API_KEY, "metric", "vi").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    WeatherResponse w = response.body();
                    tvTemperature.setText(String.format("%.0f", w.main.temp));
                    tvHumidity.setText("Độ ẩm: " + (int)w.main.humidity + "%");
                    tvWindSpeed.setText("Sức gió: " + w.wind.speed + " m/s");
                    if (tvCityName != null) tvCityName.setText(w.name);
                    if (!w.weather.isEmpty()) {
                        tvWeatherDesc.setText(w.weather.get(0).description);
                        Glide.with(HomeFragment.this).load("https://openweathermap.org/img/wn/" + w.weather.get(0).icon + "@2x.png").into(ivWeatherIcon);
                    }
                }
            }
            @Override public void onFailure(Call<WeatherResponse> call, Throwable t) {}
        });
    }

    private void loadUserProfile() {
        RetrofitClient.getInstance(getContext()).getUserApi().getProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    tvUsername.setText("Hi, " + response.body().name);
                    Glide.with(HomeFragment.this).load(response.body().avatarUrl).placeholder(R.drawable.account_icon).into(ivAvatar);
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationHelper != null) locationHelper.stopLocationUpdates();
    }
}