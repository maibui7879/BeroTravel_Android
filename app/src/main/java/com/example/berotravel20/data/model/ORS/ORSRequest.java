package com.example.berotravel20.data.model.ORS;

public class ORSRequest {
    public double[][] coordinates; // Mảng 2 chiều chứa toạ độ [[lng, lat], [lng, lat]]
    public String language;        // Ngôn ngữ (vi)

    public ORSRequest(double[][] coordinates, String language) {
        this.coordinates = coordinates;
        this.language = language;
    }
}