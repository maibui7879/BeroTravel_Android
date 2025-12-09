package com.example.berotravel20.data.model.Place;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaceResponse {
    public int total;
    public int page;
    public int totalPages;

    // Đây mới là cái list bạn cần
    @SerializedName("data")
    public List<Place> data;
}