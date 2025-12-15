package com.example.berotravel20.data.model.Place;

import com.example.berotravel20.data.model.PlaceStatus.PlaceStatus;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Place {
    @SerializedName("_id")
    public String id;

    public String name;
    public String address;
    public double latitude;
    public double longitude;
    public String description;
    public String category;

    @SerializedName("price")
    public double price;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("img_set")
    public List<String> imgSet;

    // --- Các trường mới cập nhật theo JSON ---
    @SerializedName("favorite_count")
    public int favoriteCount;

    @SerializedName("updated_by")
    public String updatedBy;

    @SerializedName("distance")
    public Double distance;

    public String updatedAt;

    // Object status được lồng bên trong
    public PlaceStatus status;

    // --- Class Request dùng cho việc Tạo/Update (Không đổi) ---
    public static class Request {
        public String name;
        public String address;
        public double latitude;
        public double longitude;
        public String description;
        public String category;
        @SerializedName("image_url")
        public String imageUrl;
        @SerializedName("img_set")
        public List<String> imgSet;
        public Contact contact;
    }

    public static class Contact {
        public String phone;
        public String email;
    }
}