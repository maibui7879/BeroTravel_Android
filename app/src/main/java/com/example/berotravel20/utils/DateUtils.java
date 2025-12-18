package com.example.berotravel20.utils; // Thay bằng package của bạn

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    // Thêm chữ 'static' để gọi được luôn mà không cần 'new DateUtils()'
    public static String formatTimeLegacy(String rawDate) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = parser.parse(rawDate);

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            return "Vừa xong";
        }
    }
}