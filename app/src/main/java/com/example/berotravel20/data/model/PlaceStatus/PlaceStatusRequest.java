package com.example.berotravel20.data.model.PlaceStatus;

import com.google.gson.annotations.SerializedName;

public class PlaceStatusRequest {
    @SerializedName("place_id")
    public String placeId;

    @SerializedName("initial_status")
    public String initialStatus; // "open", "closed"

    @SerializedName("opening_time")
    public String openingTime; // ex: "08:00"

    @SerializedName("closing_time")
    public String closingTime; // ex: "22:00"

    @SerializedName("available_status")
    public String availableStatus; // "available", "full"

    @SerializedName("available_rooms")
    public int availableRooms;

    public double price;
    public String contact;

    // Constructor tiện lợi
    public PlaceStatusRequest(String placeId, String initialStatus, String openingTime, String closingTime, String availableStatus, int availableRooms, double price, String contact) {
        this.placeId = placeId;
        this.initialStatus = initialStatus;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.availableStatus = availableStatus;
        this.availableRooms = availableRooms;
        this.price = price;
        this.contact = contact;
    }
}