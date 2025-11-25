package com.example.berotravel20.data.models.Place;

import com.example.berotravel20.data.models.PlaceStatus.PlaceStatus;

import java.util.List;

public class Place {
    private String _id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String description;
    private String category;
    private String image_url;
    private int __v;
    private int favorite_count;
    private List<String> img_set;
    private String updatedAt;
    private String updated_by;
    private PlaceStatus status;

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return image_url;
    }

    public int getV() {
        return __v;
    }

    public int getFavoriteCount() {
        return favorite_count;
    }

    public List<String> getImgSet() {
        return img_set;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updated_by;
    }

    public PlaceStatus getStatus() {
        return status;
    }
}
