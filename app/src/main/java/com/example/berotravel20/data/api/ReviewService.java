package com.example.berotravel20.data.api;

import com.example.berotravel20.data.models.Review.RatingResponse;
import com.example.berotravel20.data.models.Review.Review;
import com.example.berotravel20.data.models.Review.ReviewRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReviewService {

    @GET("reviews/count")
    Call<Integer> getReviewCount();

    @GET("reviews/top")
    Call<List<Review>> getTopReviews();

    @GET("reviews/{placeId}")
    Call<List<Review>> getReviewsByPlace(@Path("placeId") String placeId);

    @POST("reviews/{placeId}")
    Call<Review> createReview(
            @Path("placeId") String placeId,
            @Body ReviewRequest request
    );

    @GET("reviews/{placeId}/rating")
    Call<RatingResponse> getPlaceRating(@Path("placeId") String placeId);

    @PUT("reviews/{id}")
    Call<Review> updateReview(
            @Path("id") String reviewId,
            @Body ReviewRequest request
    );

    @DELETE("reviews/{id}")
    Call<Void> deleteReview(@Path("id") String reviewId);
}
