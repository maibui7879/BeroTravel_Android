package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.ORSService;
import com.example.berotravel20.data.model.ORS.ORSRequest; // Import mới
import com.example.berotravel20.data.model.ORS.ORSResponse;
import com.example.berotravel20.data.model.ORS.Step;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RouteRepository {
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjVlMjMxNjJjNGViMTQyZjc4ZjlmMzk5YzRkNTIxM2FmIiwiaCI6Im11cm11cjY0In0=";

    private final ORSService orsService;

    public interface RouteCallback {
        void onSuccess(List<LatLng> path, List<Step> steps, double distance, double duration);
        void onError(String message);
    }

    public RouteRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        orsService = retrofit.create(ORSService.class);
    }

    public void getRoute(String profile, double startLat, double startLng, double endLat, double endLng, RouteCallback callback) {
        // 1. Tạo mảng toạ độ [[startLng, startLat], [endLng, endLat]]
        // Lưu ý: ORS luôn dùng thứ tự: KINH ĐỘ trước, VĨ ĐỘ sau
        double[][] coords = new double[][] {
                {startLng, startLat},
                {endLng, endLat}
        };

        // 2. Tạo Body request với ngôn ngữ "vi"
        ORSRequest requestBody = new ORSRequest(coords, "vi");

        // 3. Gọi API POST
        orsService.getDirections(profile, ORS_API_KEY, requestBody).enqueue(new Callback<ORSResponse>() {
            @Override
            public void onResponse(Call<ORSResponse> call, Response<ORSResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ORSResponse res = response.body();

                    if (res.features != null && !res.features.isEmpty()) {
                        // Xử lý Geometry (Vẽ đường)
                        List<List<Double>> rawCoords = res.features.get(0).geometry.coordinates;
                        List<LatLng> path = new ArrayList<>();
                        for (List<Double> coord : rawCoords) {
                            path.add(new LatLng(coord.get(1), coord.get(0)));
                        }

                        // Xử lý Steps (Chỉ dẫn) & Summary
                        List<Step> steps = new ArrayList<>();
                        double totalDist = 0;
                        double totalDur = 0;

                        if (res.features.get(0).properties != null) {
                            if (res.features.get(0).properties.segments != null && !res.features.get(0).properties.segments.isEmpty()) {
                                steps = res.features.get(0).properties.segments.get(0).steps;
                            }
                            if (res.features.get(0).properties.summary != null) {
                                totalDist = res.features.get(0).properties.summary.distance;
                                totalDur = res.features.get(0).properties.summary.duration;
                            }
                        }

                        callback.onSuccess(path, steps, totalDist, totalDur);
                    } else {
                        callback.onError("Không tìm thấy đường đi");
                    }
                } else {
                    callback.onError("Lỗi API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ORSResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}