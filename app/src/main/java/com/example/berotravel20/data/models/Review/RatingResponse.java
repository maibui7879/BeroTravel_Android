package com.example.berotravel20.data.models.Review;

import java.util.Map;

public class RatingResponse {
    private double average;
    private int totalVotes;
    private Map<String, Integer> distribution;

    public double getAverage() {
        return average;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public Map<String, Integer> getDistribution() {
        return distribution;
    }
}

