package com.example.berotravel20.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MapUtils {

    // Hàm fix lỗi "rẽ rẽ"
    public static String cleanInstruction(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String s = raw.trim();
        s = s.replaceAll("(?i)rẽ\\s+rẽ", "Rẽ");
        s = s.replaceAll("(?i)đi\\s+đi", "Đi");
        s = s.replaceAll("(?i)quay\\s+đầu\\s+quay\\s+đầu", "Quay đầu");
        if (!s.isEmpty()) {
            s = s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return s;
    }

    // Đổi dp sang px
    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    // Ẩn bàn phím
    public static void hideKeyboard(Activity activity) {
        View v = activity.getCurrentFocus();
        if (v != null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}