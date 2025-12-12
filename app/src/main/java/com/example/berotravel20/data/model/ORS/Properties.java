package com.example.berotravel20.data.model.ORS;
import java.util.List;

public class Properties {
    public List<Segment> segments;
    public Summary summary; // Chứa tổng thời gian, khoảng cách

    public static class Summary {
        public double distance; // mét
        public double duration; // giây
    }
}