package com.example.berotravel20.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryUtils {

    // Dùng LinkedHashMap để giữ đúng thứ tự bạn đã liệt kê
    public static final Map<String, String> CATEGORY_MAP = new LinkedHashMap<>();

    static {
        CATEGORY_MAP.put("restaurant", "Nhà hàng");
        CATEGORY_MAP.put("other", "Khác");
        CATEGORY_MAP.put("hotel", "Khách sạn");
        CATEGORY_MAP.put("cafe", "Quán cà phê");
        CATEGORY_MAP.put("parking", "Bãi đậu xe");
        CATEGORY_MAP.put("school", "Trường học");
        CATEGORY_MAP.put("fast_food", "Đồ ăn nhanh");
        CATEGORY_MAP.put("supermarket", "Siêu thị");
        CATEGORY_MAP.put("memorial", "Đài tưởng niệm");
        CATEGORY_MAP.put("swimming", "Bơi lội");
        CATEGORY_MAP.put("university", "Đại học");
        CATEGORY_MAP.put("attraction", "Điểm tham quan");
        CATEGORY_MAP.put("place_of_worship", "Nơi thờ phụng");
        CATEGORY_MAP.put("clinic", "Phòng khám");
        CATEGORY_MAP.put("clothes", "Cửa hàng quần áo");
        CATEGORY_MAP.put("pub", "Quán rượu (Pub)");
        CATEGORY_MAP.put("bar", "Quán Bar");
        CATEGORY_MAP.put("atm", "Cây ATM");
        CATEGORY_MAP.put("convenience", "Cửa hàng tiện lợi");
        CATEGORY_MAP.put("pharmacy", "Hiệu thuốc");
        CATEGORY_MAP.put("hostel", "Nhà nghỉ tập thể");
        CATEGORY_MAP.put("electronics", "Điện tử");
        CATEGORY_MAP.put("guest_house", "Nhà khách");
        CATEGORY_MAP.put("tree", "Cây cối / Cảnh quan");
        CATEGORY_MAP.put("gallery", "Phòng trưng bày");
        CATEGORY_MAP.put("archaeological_site", "Khu khảo cổ");
        CATEGORY_MAP.put("ruins", "Tàn tích");
        CATEGORY_MAP.put("hospital", "Bệnh viện");
        CATEGORY_MAP.put("marketplace", "Chợ");
        CATEGORY_MAP.put("kindergarten", "Nhà trẻ");
        CATEGORY_MAP.put("bakery", "Tiệm bánh");
        CATEGORY_MAP.put("sports_centre", "Trung tâm thể thao");
        CATEGORY_MAP.put("monument", "Tượng đài");
        CATEGORY_MAP.put("dentist", "Nha sĩ");
        CATEGORY_MAP.put("museum", "Bảo tàng");
        CATEGORY_MAP.put("furniture", "Nội thất");
        CATEGORY_MAP.put("fitness_centre", "Trung tâm thể hình");
        CATEGORY_MAP.put("travel_agency", "Đại lý du lịch");
        CATEGORY_MAP.put("language_school", "Trường ngôn ngữ");
        CATEGORY_MAP.put("mall", "Trung tâm mua sắm");
        CATEGORY_MAP.put("beverages", "Đồ uống");
        CATEGORY_MAP.put("motel", "Nhà nghỉ (Motel)");
        CATEGORY_MAP.put("doctors", "Bác sĩ");
        CATEGORY_MAP.put("swimming_pool", "Hồ bơi");
        CATEGORY_MAP.put("department_store", "Cửa hàng bách hóa");
        CATEGORY_MAP.put("playground", "Sân chơi");
        CATEGORY_MAP.put("viewpoint", "Điểm ngắm cảnh");
        CATEGORY_MAP.put("internet_cafe", "Tiệm Internet");
        CATEGORY_MAP.put("castle", "Lâu đài");
        CATEGORY_MAP.put("driving_school", "Trường dạy lái xe");
        CATEGORY_MAP.put("pitch", "Sân bóng");
        CATEGORY_MAP.put("park", "Công viên");
        CATEGORY_MAP.put("bus_station", "Trạm xe buýt");
        CATEGORY_MAP.put("gymnastics", "Thể dục dụng cụ");
        CATEGORY_MAP.put("sports", "Thể thao");
        CATEGORY_MAP.put("tomb", "Lăng mộ");
        CATEGORY_MAP.put("dojo", "Võ đường");
        CATEGORY_MAP.put("boxing;kickboxing", "Boxing / Kickboxing");
        CATEGORY_MAP.put("fitness_station", "Trạm tập thể dục");
        CATEGORY_MAP.put("shelter", "Nơi trú ẩn");
        CATEGORY_MAP.put("music_school", "Trường nhạc");
        CATEGORY_MAP.put("soccer", "Bóng đá");
        CATEGORY_MAP.put("prep_school", "Trường dự bị");
        CATEGORY_MAP.put("running;cycling", "Chạy bộ / Đạp xe");
        CATEGORY_MAP.put("aircraft", "Máy bay");
        CATEGORY_MAP.put("wayside_shrine", "Miếu thờ ven đường");
        CATEGORY_MAP.put("college", "Cao đẳng");
    }

    /**
     * Lấy tên hiển thị tiếng Việt từ key server trả về.
     * Ví dụ: getLabel("fast_food") -> "Đồ ăn nhanh"
     */
    public static String getLabel(String key) {
        if (key == null) return "";
        // Nếu không tìm thấy key thì trả về chính nó hoặc chuỗi mặc định
        return CATEGORY_MAP.containsKey(key) ? CATEGORY_MAP.get(key) : key;
    }
}