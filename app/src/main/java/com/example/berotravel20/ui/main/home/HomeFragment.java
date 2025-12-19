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
import com.example.berotravel20.ui.main.place.PlaceFragment;
import com.example.berotravel20.ui.map.LocationHelper;
import com.example.berotravel20.ui.map.MapActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends BaseFragment {

    private TextView tvUsername, tvTemperature, tvWeatherDesc, tvHumidity, tvWindSpeed, tvCityName;
    private TextView tvSuggestedTitle;
    private ImageView ivAvatar, ivWeatherIcon;
    private EditText etSearch;
    private View loadingLayout, mainContent;

    private PlaceRepository placeRepository;
    private LocationHelper locationHelper;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private boolean isLocationFound = false;

    private MapPlaceAdapter suggestAdapter;
    private MapPlaceAdapter hotelAdapter;
    private MapPlaceAdapter restaurantAdapter;

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
            setupLocationLogic(true);
        } else {
            hideLoadingLayout();
            loadAllInitialPlaces();
            setupLocationLogic(false);
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

        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearchLogic();
                    return true;
                }
                return false;
            });

            etSearch.setOnTouchListener((v, event) -> {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (etSearch.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                        if (event.getRawX() >= (etSearch.getRight() - etSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - etSearch.getPaddingEnd() - 30)) {
                            performSearchLogic();
                            return true;
                        }
                    }
                }
                return false;
            });
        }

        View secSuggest = view.findViewById(R.id.secSuggested);
        if (secSuggest != null) {
            tvSuggestedTitle = secSuggest.findViewById(R.id.tvSectionTitle);
            if (tvSuggestedTitle != null) tvSuggestedTitle.setText("Khám phá địa điểm");
        }

        view.findViewById(R.id.btnNotification).setOnClickListener(v ->
                replaceFragment(NotificationFragment.newInstance())
        );
    }

    private void performSearchLogic() {
        String query = etSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            intent.putExtra("SEARCH_QUERY", query);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Vui lòng nhập từ khóa", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAdapters(View view) {
        MapPlaceAdapter.OnItemClickListener listener = createListener();

        // 1. Suggested (Sử dụng item_place_vertical)
        View secSuggest = view.findViewById(R.id.secSuggested);
        if (secSuggest != null) {
            RecyclerView rvSuggest = secSuggest.findViewById(R.id.rv_home_list);
            rvSuggest.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            suggestAdapter = new MapPlaceAdapter(getContext(), R.layout.item_place_vertical, listener);
            rvSuggest.setAdapter(suggestAdapter);
        }

        // 2. Hotels (Sử dụng item ngang mặc định - item_place_result)
        hotelAdapter = new MapPlaceAdapter(getContext(), listener);
        setupSection(view.findViewById(R.id.secHotels), "Khách sạn gần bạn",
                LinearLayoutManager.VERTICAL, hotelAdapter);

        // 3. Restaurants (SỬA LẠI: Sử dụng item_place_vertical giống Suggested)
        restaurantAdapter = new MapPlaceAdapter(getContext(), R.layout.item_place_vertical, listener);
        setupSection(view.findViewById(R.id.secRestaurants), "Nhà hàng ngon gần bạn",
                LinearLayoutManager.HORIZONTAL, restaurantAdapter);
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
            if (id == R.id.btnCatPark) { category = "park"; title = "Công viên quanh đây"; }
            else if (id == R.id.btnCatRestaurant) { category = "restaurant"; title = "Nhà hàng quanh đây"; }
            else if (id == R.id.btnCatAttraction) { category = "tourist_attraction"; title = "Điểm tham quan quanh đây"; }
            else if (id == R.id.btnCatHotel) { category = "hotel"; title = "Khách sạn quanh đây"; }
            else if (id == R.id.btnCatBar) { category = "bar"; title = "Quán bar & Pub"; }

            if (tvSuggestedTitle != null) tvSuggestedTitle.setText(title);
            filterSuggestionByCategory(category);
        };

        view.findViewById(R.id.btnCatPark).setOnClickListener(catListener);
        view.findViewById(R.id.btnCatRestaurant).setOnClickListener(catListener);
        view.findViewById(R.id.btnCatAttraction).setOnClickListener(catListener);
        view.findViewById(R.id.btnCatHotel).setOnClickListener(catListener);
        view.findViewById(R.id.btnCatBar).setOnClickListener(catListener);
    }

    private void setupLocationLogic(boolean showLoadingUI) {
        if (showLoadingUI) {
            loadingLayout.setVisibility(View.VISIBLE);
            mainContent.setVisibility(View.INVISIBLE);
        }

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
            if (showLoadingUI) hideLoadingLayout();
            loadAllInitialPlaces();
        }
    }

    private void handleLocationFound(android.location.Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();
        isLocationFound = true;
        hideLoadingLayout();
        fetchWeather(currentLat, currentLng);
        loadAllInitialPlaces();
        loadNearbySections(currentLat, currentLng);
    }

    private void hideLoadingLayout() {
        if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
        if (mainContent != null) mainContent.setVisibility(View.VISIBLE);
    }

    private void loadAllInitialPlaces() {
        placeRepository.getAllPlaces(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                if (isAdded() && data != null) suggestAdapter.setData(data);
            }
            @Override public void onError(String msg) { Log.e("Home", msg); }
        });
    }

    private void loadNearbySections(double lat, double lng) {
        placeRepository.searchNearby(lat, lng, 5000, null, "hotel", 1, 10, new DataCallback<PlaceResponse>() {
            @Override public void onSuccess(PlaceResponse d) { if (isAdded()) hotelAdapter.setData(d.data); }
            @Override public void onError(String msg) {}
        });
        placeRepository.searchNearby(lat, lng, 5000, null, "restaurant", 1, 10, new DataCallback<PlaceResponse>() {
            @Override public void onSuccess(PlaceResponse d) { if (isAdded()) restaurantAdapter.setData(d.data); }
            @Override public void onError(String msg) {}
        });
    }

    private void filterSuggestionByCategory(String category) {
        if (suggestAdapter != null) suggestAdapter.clearData();
        placeRepository.searchNearby(currentLat, currentLng, 5000, null, category, 1, 20, new DataCallback<PlaceResponse>() {
            @Override
            public void onSuccess(PlaceResponse data) {
                if (isAdded() && data != null) suggestAdapter.setData(data.data);
            }
            @Override public void onError(String msg) {
                if (isAdded()) Toast.makeText(getContext(), "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MapPlaceAdapter.OnItemClickListener createListener() {
        return new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onFavoriteClick(Place p) {
                if (suggestAdapter != null) suggestAdapter.toggleFavoriteLocal(p.id);
                if (hotelAdapter != null) hotelAdapter.toggleFavoriteLocal(p.id);
                if (restaurantAdapter != null) restaurantAdapter.toggleFavoriteLocal(p.id);
            }

            @Override
            public void onItemClick(Place p) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.base_container, PlaceFragment.newInstance(p.id))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDirectionClick(Place p) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putExtra("ACTION_TYPE", "DIRECT_TO_PLACE");
                intent.putExtra("TARGET_LAT", p.latitude);
                intent.putExtra("TARGET_LNG", p.longitude);
                intent.putExtra("TARGET_NAME", p.name);
                startActivity(intent);
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
                    tvUsername.setText("Xin chào, " + response.body().name);
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