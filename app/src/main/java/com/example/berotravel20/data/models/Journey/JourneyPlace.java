package com.example.berotravel20.data.models.Journey;

import com.example.berotravel20.data.models.Place.Place;

public class JourneyPlace {
    private String _id;
    private boolean visited;
    private Place place;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}
