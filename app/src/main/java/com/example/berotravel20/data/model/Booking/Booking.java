package com.example.berotravel20.data.model.Booking;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Booking {
    @SerializedName("_id")
    public String id;

    // Hứng object User chi tiết
    public User user;

    // Hứng object Place chi tiết (Có thể null nếu xóa địa điểm)
    public Place place;

    public int numberOfPeople;
    public String bookingDateTime;
    public String checkoutDateTime;

    public double totalPrice; // Dùng double vì JSON trả về số thực (ví dụ: 152083.33)
    public boolean isConfirmed;
    public boolean isPaid;

    // --- Class con để hứng thông tin Địa điểm ---
    public static class Place {
        @SerializedName("_id") public String id;
        public String name;
        public String address;

        @SerializedName("image_url")
        public String imageUrl; // Khớp với "image_url" trong JSON

        public double price;
        public String category;
    }

    // --- Class con để hứng thông tin Người dùng ---
    public static class User {
        @SerializedName("_id") public String id;
        public String name;
        public String email;
    }

    // --- Class dùng để gửi yêu cầu đặt phòng (Gửi ID dạng String) ---
    public static class Request {
        public String place; // Khi gửi đi thì Backend nhận ID (String)
        public int numberOfPeople;
        public String bookingDateTime;
        public String checkoutDateTime;

        public Request(String placeId, int n, String start, String end) {
            this.place = placeId;
            this.numberOfPeople = n;
            this.bookingDateTime = start;
            this.checkoutDateTime = end;
        }
    }
}