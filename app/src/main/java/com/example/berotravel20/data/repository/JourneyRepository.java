package com.example.berotravel20.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.berotravel20.data.api.JourneyApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.data.remote.RetrofitClient;
import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JourneyRepository {
    private static final String TAG = "API_JOURNEY";
    private final JourneyApiService apiService;

    public JourneyRepository(Context context) {
        // Sử dụng RetrofitClient bản mới nhất để tự động gắn Token
        this.apiService = RetrofitClient.getInstance(context).getJourneyApi();
    }

    // --- 1. Lấy danh sách Journey ---
    public void getJourneys(DataCallback<List<Journey>> callback) {
        apiService.getJourneys().enqueue(new Callback<Journey.Response>() {
            @Override
            public void onResponse(Call<Journey.Response> call, Response<Journey.Response> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError(parseErrorBody(response));
                }
            }
            @Override public void onFailure(Call<Journey.Response> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- 2. Lấy chi tiết Journey ---
    public void getJourneyDetail(String id, DataCallback<Journey> callback) {
        Log.d(TAG, "Đang gọi chi tiết ID: " + id);
        apiService.getJourneyById(id).enqueue(new Callback<Journey.DetailResponse>() {
            @Override
            public void onResponse(Call<Journey.DetailResponse> call, Response<Journey.DetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Journey actualJourney = response.body().data;
                    if (actualJourney != null) {
                        Log.d(TAG, "Thành công bóc tách dữ liệu Journey");
                        callback.onSuccess(actualJourney);
                    } else {
                        callback.onError("Dữ liệu hành trình trống");
                    }
                } else {
                    callback.onError(parseErrorBody(response));
                }
            }
            @Override public void onFailure(Call<Journey.DetailResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- 3. Tạo mới Journey ---
    public void createJourney(List<String> places, DataCallback<Void> callback) {
        apiService.createJourney(new Journey.Request(places)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(parseErrorBody(response));
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- 4. Cập nhật trạng thái (Suspended/Finished) ---
    public void updateStatus(String id, String status, DataCallback<Void> callback) {
        apiService.updateStatus(id, new Journey.StatusRequest(status)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(parseErrorBody(response));
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- 5. Check-in (Mark visited) ---
    public void markPlaceVisited(String journeyId, String placeId, DataCallback<Void> callback) {
        Log.d(TAG, "Calling API Check-in: Journey=" + journeyId + ", Place=" + placeId);

        apiService.markAsVisited(journeyId, placeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Check-in thành công!");
                    callback.onSuccess(null);
                } else {
                    String errorMsg = parseErrorBody(response);
                    Log.e(TAG, "Check-in thất bại: " + errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng Check-in: " + t.getMessage());
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- 6. Cập nhật danh sách địa điểm (Dùng cho Thêm/Xóa địa điểm) ---
    public void updateJourneyPlaces(String journeyId, List<String> newPlaceIds, DataCallback<Void> callback) {
        apiService.updateJourney(journeyId, new Journey.Request(newPlaceIds)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(parseErrorBody(response));
            }

            // Đã sửa lỗi chính tả ở đây: onFailureBody -> onFailure
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- 7. Xóa Journey (Bổ sung cho đủ tính năng) ---
    public void deleteJourney(String id, DataCallback<Void> callback) {
        apiService.deleteJourney(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(parseErrorBody(response));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Hàm phụ trợ để đọc nội dung lỗi từ Server (JSON body)
     */
    private String parseErrorBody(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorContent = response.errorBody().string();
                return "Lỗi " + response.code() + ": " + errorContent;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Lỗi Server: " + response.code();
    }
}