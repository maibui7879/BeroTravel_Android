package com.example.berotravel20.models;

public class AddPlaceRequest {
    private String placeId;
    private String location;

    private String journeyId;

    private String startDate;
    private String endDate;
    private boolean isNewJourney;

    private String startTime;
    private String endTime;

    public AddPlaceRequest(String placeId, String location) {
        this.placeId = placeId;
        this.location = location;
    }

    public AddPlaceRequest(String placeId, String location, String journeyId) {
        this.placeId = placeId;
        this.location = location;
        this.journeyId = journeyId;
    }

    public AddPlaceRequest(String placeId, String location, String journeyId, String startTime, String endTime) {
        this.placeId = placeId;
        this.location = location;
        this.journeyId = journeyId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AddPlaceRequest(String placeId, String location, String start, String end) {
        this.placeId = placeId;
        this.location = location;
        this.startDate = start;
        this.endDate = end;
        this.isNewJourney = true;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getLocation() {
        return location;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
