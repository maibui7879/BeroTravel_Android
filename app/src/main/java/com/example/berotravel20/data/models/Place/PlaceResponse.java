package com.example.berotravel20.data.models.Place;


import com.example.berotravel20.data.models.PlaceStatus.PlaceStatus;

import java.util.List;

public class PlaceResponse {
    private String _id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String description;
    private String category;
    private String image_url;
    private int favorite_count;
    private List<String> img_set;
    private PlaceStatus status;

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }
    public int getFavorite_count() { return favorite_count; }
    public void setFavorite_count(int favorite_count) { this.favorite_count = favorite_count; }
    public List<String> getImg_set() { return img_set; }
    public void setImg_set(List<String> img_set) { this.img_set = img_set; }
    public PlaceStatus getStatus() { return status; }
    public void setStatus(PlaceStatus status) { this.status = status; }
}

