package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.BookingApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Booking.Booking;
import com.example.berotravel20.data.remote.RetrofitClient;

import java.util.List;

public class BookingRepository extends BaseRepository {
    private BookingApiService api = RetrofitClient.getInstance().getBookingApi();

    public void getBookings(String userId, DataCallback<List<Booking>> callback) {
        makeCall(api.getBookings(userId), callback);
    }

    public void createBooking(Booking.Request req, DataCallback<Void> callback) {
        makeCall(api.createBooking(req), callback);
    }
}
