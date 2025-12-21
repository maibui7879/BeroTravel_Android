package com.example.berotravel20.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static String getTimeAgo(String createdAt) {
        if (createdAt == null) return "";

        // Định dạng thời gian từ MongoDB/NodeJS thường là ISO 8601
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            long time = sdf.parse(createdAt).getTime();
            long now = System.currentTimeMillis();
            long diff = now - time;

            if (diff < 60000) return "Vừa xong";
            if (diff < 3600000) return (diff / 60000) + " phút trước";
            if (diff < 86400000) return (diff / 3600000) + " giờ trước";
            if (diff < 2592000000L) return (diff / 86400000) + " ngày trước";

            // Nếu quá 1 tháng thì hiện ngày cụ thể
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return displayFormat.format(new Date(time));
        } catch (Exception e) {
            return "";
        }
    }
}