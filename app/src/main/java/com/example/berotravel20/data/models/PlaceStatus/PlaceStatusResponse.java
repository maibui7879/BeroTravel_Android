package com.example.berotravel20.data.models.PlaceStatus;

public class PlaceStatusResponse {
    private String _id;
    private String place_id;
    private String initial_status;
    private String opening_time;
    private String closing_time;
    private String available_status;
    private double price;
    private String contact;
    private String createdAt;
    private String updatedAt;
    private int __v;

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getPlace_id() { return place_id; }
    public void setPlace_id(String place_id) { this.place_id = place_id; }

    public String getInitial_status() { return initial_status; }
    public void setInitial_status(String initial_status) { this.initial_status = initial_status; }

    public String getOpening_time() { return opening_time; }
    public void setOpening_time(String opening_time) { this.opening_time = opening_time; }

    public String getClosing_time() { return closing_time; }
    public void setClosing_time(String closing_time) { this.closing_time = closing_time; }

    public String getAvailable_status() { return available_status; }
    public void setAvailable_status(String available_status) { this.available_status = available_status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int get__v() { return __v; }
    public void set__v(int __v) { this.__v = __v; }
}
