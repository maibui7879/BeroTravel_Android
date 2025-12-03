package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Booking.Booking;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface BookingApiService {
    @GET("/api/bookings/user/{userId}")
    Call<List<Booking>> getBookings(@Path("userId") String userId);

    @POST("/api/bookings")
    Call<Void> createBooking(@Body Booking.Request request);

    @PUT("/api/bookings/{id}")
    Call<Void> updateBooking(@Path("id") String id, @Body Booking.Request request);

    @PATCH("/api/bookings/{id}/pay")
    Call<Void> payBooking(@Path("id") String id);
}
