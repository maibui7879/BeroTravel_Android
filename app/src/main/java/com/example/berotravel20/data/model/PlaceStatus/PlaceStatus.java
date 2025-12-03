package com.example.berotravel20.data.model.PlaceStatus;

import com.google.gson.annotations.SerializedName;

public class PlaceStatus {
    @SerializedName("_id")
    public String id; // ID của status object (vd: 692275ec...)

    @SerializedName("place_id")
    public String placeId; // ID của địa điểm reference

    @SerializedName("initial_status")
    public String initialStatus; // "open", "closed"

    @SerializedName("opening_time")
    public String openingTime;

    @SerializedName("closing_time")
    public String closingTime;

    @SerializedName("available_status")
    public String availableStatus; // "available", "full"

    @SerializedName("available_rooms")
    public int availableRooms;

    public double price;
    public String contact;

    // Các trường timestamp (nếu cần dùng)
    public String createdAt;
    public String updatedAt;
}