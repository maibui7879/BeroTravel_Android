package com.example.berotravel20.data.models.Journey;

import java.util.List;

public class JourneyRequest {
    private List<String> places;

    public JourneyRequest(List<String> places) {
        this.places = places;
    }

    public List<String> getPlaces() {
        return places;
    }

    public void setPlaces(List<String> places) {
        this.places = places;
    }
}

