package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Booking.Booking;
import com.example.berotravel20.data.model.Booking.BookingCreateResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface BookingApiService {
    @GET("/api/bookings/user/{userId}")
    Call<List<Booking>> getBookings(@Path("userId") String userId);

    @POST("/api/bookings")
        // Sử dụng model Response mới ở đây
    Call<BookingCreateResponse> createBooking(@Body Booking.Request request);

    @PUT("/api/bookings/{id}")
    Call<Void> updateBooking(@Path("id") String id, @Body Booking.Request request);

    @DELETE("/api/bookings/{id}")
    Call<Void> deleteBooking(@Path("id") String id);
    @PATCH("/api/bookings/{id}/pay")
    Call<Void> payBooking(@Path("id") String id);
}
