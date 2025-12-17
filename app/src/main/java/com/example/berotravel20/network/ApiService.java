package com.example.berotravel20.network;

import com.example.berotravel20.models.PlaceResponse;
import com.example.berotravel20.models.ReviewResponse;
import com.example.berotravel20.models.UserResponse;
import com.example.berotravel20.models.LoginRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
        // Places
        @GET("places")
        Call<PlaceResponse> getPlaces(@Query("limit") int limit, @Query("category") String category);

        @GET("places/{id}")
        Call<PlaceResponse.Place> getPlaceById(@Path("id") String id);

        // Reviews
        @GET("reviews/{placeId}")
        Call<List<ReviewResponse>> getReviews(@Path("placeId") String placeId);

        // Auth
        @POST("users/login")
        Call<UserResponse> login(@Body LoginRequest request);

        @GET("users/profile")
        Call<UserResponse> getProfile();

        @POST("reviews/{placeId}")
        Call<ReviewResponse> createReview(@Path("placeId") String placeId,
                        @Body com.example.berotravel20.models.CreateReviewRequest request);

        @POST("bookings")
        Call<Void> createBooking(@Body com.example.berotravel20.models.BookingRequest request);

        @POST("reviews/{id}/vote")
        Call<Void> voteReview(@Path("id") String reviewId,
                        @Body com.example.berotravel20.models.VoteRequest request);
}
