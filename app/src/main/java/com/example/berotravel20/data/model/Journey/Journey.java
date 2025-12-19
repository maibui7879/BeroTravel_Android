package com.example.berotravel20.data.model.Journey;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Journey {
    @SerializedName("_id")
    public String id;
    public String location;
    public String startDate;
    public String endDate;
    public String status; // "ongoing", "suspended"
    public String createdAt;
    public List<JourneyPlaceWithStatus> places;

    public static class JourneyPlaceWithStatus {
        @SerializedName("_id")
        public String id;
        public PlaceDetails place;
        public boolean visited;
        public String startTime;
        public String endTime;
    }

    public static class PlaceDetails {
        @SerializedName("_id")
        public String id;
        public String name;
        public String address;
        public String image_url;
    }

    public static class Request {
        public List<String> places;

        public Request(List<String> p) {
            this.places = p;
        }
    }

    public static class StatusRequest {
        public String status;

        public StatusRequest(String s) {
            this.status = s;
        }
    }
}
