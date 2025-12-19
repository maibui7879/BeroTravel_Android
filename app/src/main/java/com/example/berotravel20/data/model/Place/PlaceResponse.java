package com.example.berotravel20.data.model.Place;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaceResponse {
    public int total;
    public int page;
    public int limit;
    public int totalPages;

    @SerializedName("data")
    public List<Place> data;
}