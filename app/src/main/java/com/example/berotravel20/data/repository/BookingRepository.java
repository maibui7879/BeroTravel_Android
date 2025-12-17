package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.BookingApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Booking.Booking;
import com.example.berotravel20.data.remote.RetrofitClient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BookingRepository extends BaseRepository {
    private final BookingApiService api = RetrofitClient.getInstance().getBookingApi();

    // Format ISO 8601 chuẩn cho Backend NestJS
    private final SimpleDateFormat isoFormat;

    public BookingRepository() {
        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Tích hợp xử lý: Nhận dữ liệu thô (long) và chuyển đổi trước khi gọi API
     */
    public void createBooking(String placeId, int people, long checkin, long checkout, DataCallback<Void> callback) {
        String startStr = isoFormat.format(new Date(checkin));
        String endStr = isoFormat.format(new Date(checkout));

        Booking.Request req = new Booking.Request(placeId, people, startStr, endStr);
        makeCall(api.createBooking(req), callback);
    }

    public void getBookings(String userId, DataCallback<List<Booking>> callback) {
        makeCall(api.getBookings(userId), callback);
    }
}