package com.example.berotravel20.data.remote;

import android.content.Context;
import android.util.Log;
import com.example.berotravel20.data.api.*;
import com.example.berotravel20.data.local.TokenManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RETROFIT_CLIENT";
    private static final String BASE_URL = "http://10.0.2.2:5000/"; // IP máy ảo Android

    private static RetrofitClient instance = null;
    private final Retrofit retrofit;

    // Cache các API Service để tránh khởi tạo lại mỗi khi gọi (Tăng hiệu năng)
    private AuthApiService authApi;
    private UserApiService userApi;
    private PlaceApiService placeApi;
    private BookingApiService bookingApi;
    private JourneyApiService journeyApi;
    private FavoriteApiService favoriteApi;
    private ReviewApiService reviewApi;

    private RetrofitClient(Context context) {
        // Khởi tạo TokenManager
        TokenManager tokenManager = TokenManager.getInstance(context);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 30s là đủ, tránh chờ quá lâu
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        // Lấy token tự động từ TokenManager
                        String token = tokenManager.getToken();

                        if (token != null && !token.isEmpty()) {
                            Log.d(TAG, "Tiêm Token vào Header: Bearer " + token);
                            builder.header("Authorization", "Bearer " + token);
                        } else {
                            Log.w(TAG, "Yêu cầu được gửi mà không có Token");
                        }

                        return chain.proceed(builder.build());
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
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
    public Retrofit getRetrofit() {
        return retrofit;
    }
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Hãy khởi tạo RetrofitClient.getInstance(context) trước!");
        }
        return instance;
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
}