package com.example.berotravel20.data.repository;

import android.content.Context;

import com.example.berotravel20.data.api.ReviewService;
import com.example.berotravel20.data.api.RetrofitClient;
import com.example.berotravel20.data.local.SessionManager;
import com.example.berotravel20.data.models.Review.RatingResponse;
import com.example.berotravel20.data.models.Review.Review;
import com.example.berotravel20.data.models.Review.ReviewRequest;

import java.util.List;

import retrofit2.Call;

public class ReviewRepository {

    private final ReviewService service;

    public ReviewRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        service = RetrofitClient.getClient(sessionManager).create(ReviewService.class);
    }

    public Call<Integer> getReviewCount() {
        return service.getReviewCount();
    }

    public Call<List<Review>> getTopReviews() {
        return service.getTopReviews();
    }

    public Call<List<Review>> getReviewsByPlace(String placeId) {
        return service.getReviewsByPlace(placeId);
    }

    public Call<Review> createReview(String placeId, ReviewRequest request) {
        return service.createReview(placeId, request);
    }

    public Call<RatingResponse> getPlaceRating(String placeId) {
        return service.getPlaceRating(placeId);
    }

    public Call<Review> updateReview(String reviewId, ReviewRequest request) {
        return service.updateReview(reviewId, request);
    }

    public Call<Void> deleteReview(String reviewId) {
        return service.deleteReview(reviewId);
    }
}
