package com.example.berotravel20.data.model.Booking;

import com.google.gson.annotations.SerializedName;

public class BookingCreateResponse {
    @SerializedName("_id")
    public String id;

    public String user;  // Ở đây là String (ID), không phải Object
    public String place; // Ở đây là String (ID)

    public int numberOfPeople;
    public String bookingDateTime;
    public String checkoutDateTime;
    public double totalPrice;
    public boolean isPaid;
    public boolean isConfirmed;
}