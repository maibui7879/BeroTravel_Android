package com.example.berotravel20.models;

import java.util.List;

public class PlaceResponse {
    public int total;
    public List<Place> data;

    public static class Place {
        public String _id;
        public String name;
        public String address;
        public String description;
        public double latitude;
        public double longitude;
        public String image_url;
        public List<String> img_set;
        public double price;
        public String category;
        public int favorite_count;
        public PlaceStatus status;
    }

    public static class PlaceStatus {
        public String available_status;
        public int price;
    }
}
