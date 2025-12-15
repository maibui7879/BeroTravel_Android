package com.example.berotravel20.models;

public class CreateReviewRequest {
    public float rating;
    public String comment;

    public CreateReviewRequest(float rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
