package com.example.berotravel20.data.model.Journey;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Journey {
    @SerializedName("_id") public String id;
    public List<String> places;
    public String status; // "ongoing", "suspended"

    public static class Request {
        public List<String> places;
        public Request(List<String> p) { this.places = p; }
    }

    public static class StatusRequest {
        public String status;
        public StatusRequest(String s) { this.status = s; }
    }
}
