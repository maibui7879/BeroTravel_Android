package com.example.berotravel20.data.model.Vote;

import com.google.gson.annotations.SerializedName;

public class Vote {
    @SerializedName("_id") public String id;
    @SerializedName("user_id") public String userId;
    @SerializedName("target_id") public String targetId;
    @SerializedName("target_type") public String targetType;
    @SerializedName("vote_type") public String voteType;

    public static class Request {
        @SerializedName("target_id") public String targetId;
        @SerializedName("target_type") public String targetType; // "review"
        @SerializedName("vote_type") public String voteType; // "upvote" | "downvote"

        public Request(String targetId, String targetType, String voteType) {
            this.targetId = targetId;
            this.targetType = targetType;
            this.voteType = voteType;
        }
    }
}