package com.example.berotravel20.data.models.PlaceStatus;

public class PlaceStatus {
    private String _id;
    private String place_id;
    private String initial_status;
    private String opening_time;
    private String closing_time;
    private String available_status;
    private double price;
    private String contact;
    private String createdAt;
    private String updatedAt;
    private int __v;

    public String getId() {
        return _id;
    }

    public String getPlaceId() {
        return place_id;
    }

    public String getInitialStatus() {
        return initial_status;
    }

    public String getOpeningTime() {
        return opening_time;
    }

    public String getClosingTime() {
        return closing_time;
    }

    public String getAvailableStatus() {
        return available_status;
    }

    public double getPrice() {
        return price;
    }

    public String getContact() {
        return contact;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getV() {
        return __v;
    }
}
