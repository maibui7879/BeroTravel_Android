package com.example.berotravel20.data.model.Journey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Journey implements Serializable {
    @SerializedName("_id") public String id;
    public String status;
    public List<JourneyPlace> places;
    public String createdAt;
    public String updatedAt;

    // --- CẤU TRÚC DỮ LIỆU ---
    public static class JourneyPlace {
        @SerializedName("_id") public String id;
        public PlaceDetail place; // Thông tin địa điểm đã được server populate
        public boolean visited;
    }

    public static class PlaceDetail {
        @SerializedName("_id") public String id;
        public String name;
        public String address;
        @SerializedName("image_url") public String imageUrl;
        public double latitude;
        public double longitude;
    }

    // --- CÁC LỚP GỬI DỮ LIỆU (REQUEST) ---
    public static class Request {
        public List<String> places;
        public Request(List<String> places) { this.places = places; }
    }

    public static class StatusRequest {
        public String status;
        public StatusRequest(String status) { this.status = status; }
    }

    // --- CÁC LỚP BÓC TÁCH JSON (RESPONSE) ---

    // Dùng cho API Danh sách: { "success": true, "data": [...] }
    public static class Response {
        public boolean success;
        public List<Journey> data;
    }

    // Dùng cho API Chi tiết: { "success": true, "data": {...} }
    // FIX LỖI: Expected BEGIN_ARRAY but was BEGIN_OBJECT
    public static class DetailResponse {
        public boolean success;
        public Journey data;
    }
}