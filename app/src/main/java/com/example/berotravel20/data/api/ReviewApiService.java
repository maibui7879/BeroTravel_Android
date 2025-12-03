package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Review.Review;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ReviewApiService {
    @GET("/api/reviews/{placeId}")
    Call<List<Review>> getReviewsByPlace(@Path("placeId") String placeId);

    @POST("/api/reviews/{placeId}")
    Call<Void> createReview(@Path("placeId") String placeId, @Body Review.Request request);

    @GET("/api/reviews/top")
    Call<List<Review>> getTopReviews();

    @PUT("/api/reviews/{id}")
    Call<Void> updateReview(@Path("id") String id, @Body Review.Request request);

    @DELETE("/api/reviews/{id}")
    Call<Void> deleteReview(@Path("id") String id);

    @GET("/api/reviews/{placeId}/rating")
    Call<Review.RatingResponse> getPlaceRating(@Path("placeId") String placeId);
}