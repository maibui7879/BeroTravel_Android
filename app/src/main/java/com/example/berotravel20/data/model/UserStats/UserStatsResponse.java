package com.example.berotravel20.data.model.Stats;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserStatsResponse {
    @SerializedName("user_id")
    public UserInfo userInfo;

    @SerializedName("edited_places")
    public PlaceGroup editedPlaces;

    @SerializedName("reviews_created")
    public ReviewGroup reviewsCreated;

    @SerializedName("votes_created")
    public VoteGroup votesCreated;

    // --- GROUP CLASSES ---
    public static class PlaceGroup {
        public int count;
        public List<StatPlace> places;
    }

    public static class ReviewGroup {
        public int count;
        public List<StatReview> reviews;
    }

    public static class VoteGroup {
        public int count;
        public List<StatVote> votes;
    }

    // --- ITEM MODELS ---
    public static class UserInfo {
        @SerializedName("_id") public String id;
        public String name;
        public String email;
        @SerializedName("avatar_url") public String avatarUrl;
    }

    public static class StatPlace {
        @SerializedName("place_id") public String placeId;
        public String name;
        @SerializedName("updated_at") public String updatedAt;
    }

    public static class StatReview {
        @SerializedName("review_id") public String reviewId;
        @SerializedName("place_id") public String placeId;
        public int rating;
        public String comment;
        @SerializedName("created_at") public String createdAt;
    }

    public static class StatVote {
        @SerializedName("vote_id") public String voteId;
        @SerializedName("target_id") public String targetId;
        @SerializedName("target_type") public String targetType;
        @SerializedName("vote_type") public String voteType;
        @SerializedName("created_at") public String createdAt;
    }
}