package com.example.berotravel20.data.models.Review;

public class Review {
    private String _id;
    private int rating;
    private String comment;
    private ReviewUser user_id;
    private String place_id;
    private String image_url;
    private int vote_score;

    public String get_id() {
        return _id;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public ReviewUser getUser_id() {
        return user_id;
    }

    public String getPlace_id() {
        return place_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public int getVote_score() {
        return vote_score;
    }
}
