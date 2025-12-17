package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.ReviewApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Review.Review;
import com.example.berotravel20.data.remote.RetrofitClient;

import java.util.List;

public class ReviewRepository extends BaseRepository {
    private ReviewApiService api = RetrofitClient.getInstance().getReviewApi();

    public void getReviewsByPlace(String placeId, DataCallback<List<Review>> callback) {
        makeCall(api.getReviewsByPlace(placeId), callback);
    }

    public void createReview(String placeId, int rating, String comment, DataCallback<Void> callback) {
        makeCall(api.createReview(placeId, new Review.Request(rating, comment)), callback);
    }

    public void getTopReviews(DataCallback<List<Review>> callback) {
        makeCall(api.getTopReviews(), callback);
    }

    public void deleteReview(String id, DataCallback<Void> callback) {
        makeCall(api.deleteReview(id), callback);
    }
    public void getPlaceRating(String placeId, DataCallback<Review.RatingResponse> callback) {
        makeCall(api.getPlaceRating(placeId), callback);
    }
}
