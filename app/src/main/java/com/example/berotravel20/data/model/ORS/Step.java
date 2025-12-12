package com.example.berotravel20.data.model.ORS;

public class Step {
    public double distance;
    public double duration;
    public int type; // Loại hướng dẫn (rẽ trái, phải...)
    public String instruction; // Câu hướng dẫn (Tiếng Việt)
    public String name; // Tên đường
    public int[] way_points;
}