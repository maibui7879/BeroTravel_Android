package com.example.berotravel20.data.model.Weather;

import java.util.List;

public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public String name; // Tên thành phố

    public class Main {
        public float temp; // Nhiệt độ
        public float humidity; // Độ ẩm
    }

    public class Weather {
        public String description; // Mô tả (nắng, mưa...)
        public String icon; // Mã icon ảnh
    }
}