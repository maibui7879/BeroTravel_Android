package com.example.berotravel20.models;

public class ReviewResponse {
    public String _id;
    public double rating;
    public String comment;
    public User user_id;
    public String createdAt;
    public int vote_score;
    public String user_vote; // "upvote", "downvote", or null

    public static class User {
        public String _id;
        public String name;
        public String avatar_url;
    }
}
