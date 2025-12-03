package com.example.berotravel20.data.model.Place;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceImagesRequest {
    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("img_set")
    public List<String> imgSet;

    public PlaceImagesRequest(String imageUrl, List<String> imgSet) {
        this.imageUrl = imageUrl;
        this.imgSet = imgSet;
    }
}