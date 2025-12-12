package com.example.berotravel20.data.repository;

import android.util.Log;

import com.example.berotravel20.data.api.ORSService;
import com.example.berotravel20.data.model.ORS.ORSResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RouteRepository {
    private static final String TAG = "RouteRepository";
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjVlMjMxNjJjNGViMTQyZjc4ZjlmMzk5YzRkNTIxM2FmIiwiaCI6Im11cm11cjY0In0="; // <--- KEY CỦA BẠN
    private final ORSService orsService;

    public interface RouteCallback {
        void onSuccess(List<LatLng> path);
        void onError(String message);
    }

    public RouteRepository() {
        Retrofit retrofitORS = new Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        orsService = retrofitORS.create(ORSService.class);
    }

    public void getRoute(double startLat, double startLng, double endLat, double endLng, RouteCallback callback) {
        // ORS format: "longitude,latitude"
        String start = startLng + "," + startLat;
        String end = endLng + "," + endLat;

        orsService.getDirections(ORS_API_KEY, start, end).enqueue(new Callback<ORSResponse>() {
            @Override
            public void onResponse(Call<ORSResponse> call, Response<ORSResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ORSResponse orsResponse = response.body();
                    if (orsResponse.features != null && !orsResponse.features.isEmpty()) {
                        List<List<Double>> rawCoords = orsResponse.features.get(0).geometry.coordinates;
                        List<LatLng> path = new ArrayList<>();

                        for (List<Double> coord : rawCoords) {
                            // ORS: [lng, lat] -> Google: LatLng(lat, lng)
                            path.add(new LatLng(coord.get(1), coord.get(0)));
                        }
                        callback.onSuccess(path);
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