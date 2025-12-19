package com.example.berotravel20.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {

    // Giảm xuống 800px thôi cho nhẹ (chuẩn HD là đủ dùng cho Avatar rồi)
    private static final int MAX_DIMENSION = 800;

    public static String uriToBase64(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (originalBitmap == null) return null;

            // 1. Thu nhỏ ảnh (Resize)
            Bitmap resizedBitmap = resizeBitmap(originalBitmap);

            // 2. Xác định loại ảnh
            String mimeType = context.getContentResolver().getType(uri);
            Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
            String prefix = "data:image/jpeg;base64,";

            // Nén mạnh tay hơn: 60% thôi (Mắt thường nhìn trên đt vẫn ok)
            int quality = 60;

            if ("image/png".equals(mimeType)) {
                compressFormat = Bitmap.CompressFormat.PNG;
                prefix = "data:image/png;base64,";
                // PNG không nén quality được, nhưng đã được resize ở trên rồi nên cũng nhẹ bớt
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(compressFormat, quality, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            // Dọn dẹp bộ nhớ
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle();
            }
            resizedBitmap.recycle();

            String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            // --- LOG KIỂM TRA DUNG LƯỢNG ---
            // Tính sơ bộ: 1 ký tự Base64 ~ 0.75 byte.
            // Ví dụ: Chuỗi dài 100,000 ký tự ~ 75KB -> OK
            Log.d("IMAGE_DEBUG", "Kích thước ảnh Base64: " + base64String.length() + " chars (~" + (base64String.length() * 0.75 / 1024) + " KB)");

            return prefix + base64String;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Nếu ảnh đã nhỏ sẵn thì thôi
        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
            return bitmap;
        }

        // Tính tỷ lệ thu nhỏ
        float ratio = Math.min((float) MAX_DIMENSION / width, (float) MAX_DIMENSION / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}