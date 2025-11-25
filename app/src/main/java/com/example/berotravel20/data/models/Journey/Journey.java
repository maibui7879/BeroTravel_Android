package com.example.berotravel20.data.models.Journey;

import java.util.List;


public class Journey {
    private String _id;
    private String user;
    private List<JourneyPlace> places;
    private String status;
    private String createdAt;
    private String updatedAt;
    private int __v;

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public List<JourneyPlace> getPlaces() { return places; }
    public void setPlaces(List<JourneyPlace> places) { this.places = places; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public int get__v() { return __v; }
    public void set__v(int __v) { this.__v = __v; }
}

