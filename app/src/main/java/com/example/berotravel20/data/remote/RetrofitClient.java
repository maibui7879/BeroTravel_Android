package com.example.berotravel20.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.berotravel20.data.api.*; // Import các API Service

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private Retrofit retrofit;
    private static final String BASE_URL = "https://7d7239d2bac8.ngrok-free.app/";

    // Constructor Private
    private RetrofitClient(Context context) {

        // Cấu hình OkHttpClient với Interceptor (Lấy từ code ApiClient của bạn)
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Thêm timeout cho chắc
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        // Lấy token trực tiếp từ SharedPreferences
                        // Lưu ý: Đảm bảo tên file "MyPrefs" và key "auth_token" khớp với lúc bạn lưu khi Login
                        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String token = prefs.getString("auth_token", null);

                        // Nếu có token thì gắn vào Header
                        if (token != null) {
                            builder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(builder.build());
                    }
                })
                .build();

        // Khởi tạo Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // Gắn client đã có interceptor
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Singleton: Khởi tạo 1 lần duy nhất từ Application
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    // Hàm gọi nhanh cho Repository (không cần truyền Context nữa)
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RetrofitClient chưa được khởi tạo trong Application!");
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
}