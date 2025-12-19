package com.example.berotravel20.data.model.Weather;

import java.util.List;

/**
 * Model hứng dữ liệu từ OpenWeatherMap API
 */
public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public Wind wind;      // Thêm để lấy thông tin sức gió
    public String name;    // Tên thành phố/vị trí

    public class Main {
        public float temp;     // Nhiệt độ
        public float humidity; // Độ ẩm (%)
    }

    public class Weather {
        public String description; // Mô tả (trời nắng, mây rải rác...)
        public String icon;        // Mã icon (01d, 02n...)
    }

    public class Wind {
        public float speed;    // Tốc độ gió (m/s)
    }
}