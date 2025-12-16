package com.example.berotravel20.models;

import java.util.List;

public class CreateJourneyRequest {
    private String location;
    private String startDate;
    private String endDate;
    private List<String> places;
    private String status;

    public CreateJourneyRequest(String location, String startDate, String endDate, List<String> places) {
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.places = places;
        this.status = "ongoing";
    }

    // Getters and Setters if needed, but Gson uses fields directly typically
    public String getLocation() {
        return location;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public List<String> getPlaces() {
        return places;
    }

    public String getStatus() {
        return status;
    }
}
