package com.example.berotravel20.models;

public class BookingRequest {
    public String place;
    public int numberOfPeople;
    public String bookingDateTime; // ISO 8601 string
    public String checkoutDateTime;

    public BookingRequest(String place, int numberOfPeople, String bookingDateTime, String checkoutDateTime) {
        this.place = place;
        this.numberOfPeople = numberOfPeople;
        this.bookingDateTime = bookingDateTime;
        this.checkoutDateTime = checkoutDateTime;
    }
}
