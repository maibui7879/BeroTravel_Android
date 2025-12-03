package com.example.berotravel20.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.berotravel20.data.api.*;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private Retrofit retrofit;
    private static final String BASE_URL = "http://10.0.2.2:5000/";

    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) instance = new RetrofitClient();
        return instance;
    }

    public AuthApiService getAuthApi() { return retrofit.create(AuthApiService.class); }
    public UserApiService getUserApi() { return retrofit.create(UserApiService.class); }
    public PlaceApiService getPlaceApi() { return retrofit.create(PlaceApiService.class); }
    public ReviewApiService getReviewApi() { return retrofit.create(ReviewApiService.class); }
    public VoteApiService getVoteApi() { return retrofit.create(VoteApiService.class); }
    public BookingApiService getBookingApi() { return retrofit.create(BookingApiService.class); }
    public JourneyApiService getJourneyApi() { return retrofit.create(JourneyApiService.class); }
    public PlaceStatusApiService getPlaceStatusApi() {
        return retrofit.create(PlaceStatusApiService.class);
    }
}