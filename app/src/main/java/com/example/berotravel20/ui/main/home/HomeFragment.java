package com.example.berotravel20.ui.main.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.adapters.MapPlaceAdapter;
import com.example.berotravel20.data.api.WeatherApiService;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Place.PlaceResponse;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.model.Weather.WeatherResponse;
import com.example.berotravel20.data.remote.RetrofitClient;
import com.example.berotravel20.ui.main.notification.NotificationActivity;
import com.example.berotravel20.ui.map.LocationHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    // 1. Khai báo biến giao diện
    private TextView tvUsername, tvCityName, tvTemperature, tvWeatherDesc;
    private ImageView ivAvatar, ivWeatherIcon;
    private RecyclerView rvSuggestedPlaces;

    // 2. Khai báo biến logic
    private LocationHelper locationHelper;
    private MapPlaceAdapter placeAdapter;

    private static final String WEATHER_API_KEY = "144fd485cfd342f1282c4b1e82446243";
    private static final String BASE_URL_WEATHER = "https://api.openweathermap.org/";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // A. ÁNH XẠ VIEW (Nối code với XML)
        tvUsername = view.findViewById(R.id.tvUsername);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        tvCityName = view.findViewById(R.id.tvCityName);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);

        rvSuggestedPlaces = view.findViewById(R.id.rvSuggestedPlaces);

        ImageView btnNotification = view.findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });

        // B. SETUP DANH SÁCH GỢI Ý (RECYCLERVIEW)
        setupRecyclerView();

        // C. GỌI CÁC HÀM XỬ LÝ
        loadUserProfile();      // 1. Lấy tên user
        setupWeatherLogic();    // 2. Lấy thời tiết
        loadSuggestedPlaces();  // 3. Lấy địa điểm gợi ý
    }

    // --- PHẦN 1: USER ---
    private void loadUserProfile() {
        RetrofitClient.getInstance(getContext()).getUserApi().getProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvUsername.setText(user.name); // Hiển thị tên
                    if (user.avatarUrl != null) {
                        Glide.with(getContext()).load(user.avatarUrl).into(ivAvatar);
                    }
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvUsername.setText("Khách tham quan");
            }
        });
    }

    // --- PHẦN 2: THỜI TIẾT (Có tọa độ chính xác) ---
    private void setupWeatherLogic() {
        locationHelper = new LocationHelper(getActivity());

        if (locationHelper.hasPermission()) {
            // Bắt đầu lấy vị trí
            locationHelper.startLocationUpdates(location -> {
                if (location != null) {
                    // Có tọa độ -> Gọi API thời tiết
                    fetchWeather(location.getLatitude(), location.getLongitude());

                    // Lấy xong thì dừng ngay cho đỡ tốn pin
                    locationHelper.stopLocationUpdates();
                }
            });
        } else {
            locationHelper.requestPermission();
            tvCityName.setText("Cần quyền vị trí");
        }
    }

    private void fetchWeather(double lat, double lon) {
        // Tạo Retrofit riêng cho Weather (Vì khác server với App)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService service = retrofit.create(WeatherApiService.class);
        service.getCurrentWeather(lat, lon, WEATHER_API_KEY, "metric", "vi")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse w = response.body();
                            tvCityName.setText(w.name);
                            tvTemperature.setText(String.format("%.0f°C", w.main.temp));

                            if (!w.weather.isEmpty()) {
                                String desc = w.weather.get(0).description;
                                tvWeatherDesc.setText(desc.substring(0, 1).toUpperCase() + desc.substring(1));

                                String iconUrl = "https://openweathermap.org/img/wn/" + w.weather.get(0).icon + "@2x.png";
                                Glide.with(getContext()).load(iconUrl).into(ivWeatherIcon);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        tvCityName.setText("Lỗi thời tiết");
                    }
                });
    }

    // --- PHẦN 3: ĐỊA ĐIỂM GỢI Ý ---
    private void setupRecyclerView() {
        rvSuggestedPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        placeAdapter = new MapPlaceAdapter(getContext(), new MapPlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Place place) {
                Toast.makeText(getContext(), "Bạn chọn: " + place.name, Toast.LENGTH_SHORT).show();
                // Sau này sẽ code chuyển màn hình ở đây
            }
            @Override
            public void onDirectionClick(Place place) {
                // Sau này code chỉ đường ở đây
            }
        });
        rvSuggestedPlaces.setAdapter(placeAdapter);
    }

    private void loadSuggestedPlaces() {
        // 1. Gọi hàm getAllPlaces() (không truyền tham số)
        RetrofitClient.getInstance(getContext()).getPlaceApi().getAllPlaces()
                .enqueue(new Callback<PlaceResponse>() { // 2. Sửa thành Callback<PlaceResponse>
                    @Override
                    public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // 3. Lấy danh sách từ bên trong PlaceResponse
                            // (Bạn kiểm tra file PlaceResponse.java xem biến chứa list tên là 'data' hay 'places' nhé.
                            // Thông thường mình thấy các bạn hay đặt là 'data' hoặc 'results')

                            // Ví dụ: Nếu trong PlaceResponse có biến: public List<Place> data;
                            List<Place> listSuggestions = response.body().data;

                            // Nếu biến tên khác thì đổi .data thành tên đó
                            if (listSuggestions != null) {
                                placeAdapter.setData(listSuggestions);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PlaceResponse> call, Throwable t) {
                        Log.e("Home", "Lỗi load places: " + t.getMessage());
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Dừng update vị trí khi thoát màn hình (An toàn tuyệt đối)
        if (locationHelper != null) locationHelper.stopLocationUpdates();
    }
}