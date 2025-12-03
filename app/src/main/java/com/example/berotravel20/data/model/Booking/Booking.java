package com.example.berotravel20.data.model.Booking;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("_id") public String id;
    public String place; // placeId
    public int numberOfPeople;
    public String bookingDateTime;
    public String checkoutDateTime;
    public boolean isConfirmed;
    public boolean isPaid;

    public static class Request {
        public String place;
        public int numberOfPeople;
        public String bookingDateTime;
        public String checkoutDateTime;

        public Request(String place, int n, String start, String end) {
            this.place = place; this.numberOfPeople = n;
            this.bookingDateTime = start; this.checkoutDateTime = end;
        }
    }
}
