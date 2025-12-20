package com.example.berotravel20.data.remote;

import android.content.Context;
import android.util.Log;

import com.example.berotravel20.data.api.*;
import com.example.berotravel20.data.local.TokenManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RETROFIT_CLIENT";

    // URL chính (Hugging Face)
    private static final String PRIMARY_URL = "https://SybauSuzuka-BeroTravel.hf.space/";
    // URL dự phòng (Localhost dành cho Emulator Android)
    private static final String FALLBACK_URL = "http://10.0.2.2:5000/";

    private static RetrofitClient instance = null;
    private final Retrofit retrofit;

    private RetrofitClient(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối tối đa 30s
                .readTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu tối đa 30s
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        // 1. Tiêm Token vào Header (giữ nguyên logic của bạn)
                        Request.Builder builder = originalRequest.newBuilder();
                        String token = tokenManager.getToken();
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        Request currentRequest = builder.build();

                        Response response = null;
                        boolean isPrimaryFailed = false;

                        // 2. Thử gọi URL Primary (Hugging Face)
                        try {
                            response = chain.proceed(currentRequest);

                            // Nếu server trả về lỗi hệ thống (5xx) hoặc không phản hồi đúng, coi như thất bại để fallback
                            if (!response.isSuccessful() && response.code() >= 500) {
                                isPrimaryFailed = true;
                            }
                        } catch (IOException e) {
                            // Lỗi kết nối (Timeout, No Internet, DNS fail...)
                            Log.e(TAG, "Primary URL thất bại: " + e.getMessage());
                            isPrimaryFailed = true;
                        }

                        // 3. Logic Fallback sang Localhost nếu Primary lỗi
                        if (isPrimaryFailed) {
                            Log.w(TAG, "Đang chuyển hướng sang Localhost: " + FALLBACK_URL);

                            if (response != null) response.close(); // Đóng kết nối cũ

                            HttpUrl fallbackBase = HttpUrl.parse(FALLBACK_URL);
                            if (fallbackBase != null) {
                                // Xây dựng URL mới nhưng giữ nguyên Path và Query của request gốc
                                HttpUrl newUrl = currentRequest.url().newBuilder()
                                        .scheme(fallbackBase.scheme())
                                        .host(fallbackBase.host())
                                        .port(fallbackBase.port())
                                        .build();

                                Request fallbackRequest = currentRequest.newBuilder()
                                        .url(newUrl)
                                        .build();

                                return chain.proceed(fallbackRequest);
                            }
                        }

                        return response;
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(PRIMARY_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Hãy khởi tạo RetrofitClient.getInstance(context) trước!");
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    // --- Danh sách API Service ---
    public AuthApiService getAuthApi() { return retrofit.create(AuthApiService.class); }
    public UserApiService getUserApi() { return retrofit.create(UserApiService.class); }
    public PlaceApiService getPlaceApi() { return retrofit.create(PlaceApiService.class); }
    public PlaceStatusApiService getPlaceStatusApi() { return retrofit.create(PlaceStatusApiService.class); }
    public ReviewApiService getReviewApi() { return retrofit.create(ReviewApiService.class); }
    public VoteApiService getVoteApi() { return retrofit.create(VoteApiService.class); }
    public BookingApiService getBookingApi() { return retrofit.create(BookingApiService.class); }
    public JourneyApiService getJourneyApi() { return retrofit.create(JourneyApiService.class); }
    public FavoriteApiService getFavoriteApi() { return retrofit.create(FavoriteApiService.class); }
    public NotificationApiService getNotificationApi(){ return retrofit.create(NotificationApiService.class);}
    public UserStatsApiService getUserStatsApi(){ return retrofit.create(UserStatsApiService.class);}
}