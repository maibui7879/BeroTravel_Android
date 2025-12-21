package com.example.berotravel20.data.model.Review;

import com.example.berotravel20.data.model.User.User;
import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("_id") public String id;
    public int rating;
    public String comment;
    @SerializedName("user_id") public User.Brief user; // User rút gọn
    @SerializedName("place_id") public String placeId;
    @SerializedName("vote_score") public int voteScore;
    @SerializedName("createdAt") public String createdAt;
    public static class Request {
        public int rating;
        public String comment;
        public Request(int r, String c) { this.rating = r; this.comment = c; }
    }

    public static class CountResponse {
        public int totalReviews;
    }

    public static class RatingResponse {
        public double average;
    }
}