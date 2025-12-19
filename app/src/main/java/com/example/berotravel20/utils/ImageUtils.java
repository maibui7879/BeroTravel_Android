package com.example.berotravel20.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {

    private static final int MAX_DIMENSION = 800; // Kích thước tối đa cho ảnh

    public static String uriToBase64(Context context, Uri uri) {
        try {
            // 1. Đọc thông số ảnh (Bounds) mà không nạp ảnh vào RAM
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(is, null, options);
            }

            // 2. Tính toán tỷ lệ thu nhỏ (inSampleSize) để nạp ảnh an toàn
            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION);
            options.inJustDecodeBounds = false;

            // 3. Nạp ảnh đã được thu nhỏ một phần vào RAM
            Bitmap sampledBitmap;
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                sampledBitmap = BitmapFactory.decodeStream(is, null, options);
            }

            if (sampledBitmap == null) return null;

            // 4. Resize chính xác về MAX_DIMENSION (Duy trì tỷ lệ)
            Bitmap finalBitmap = resizeBitmap(sampledBitmap);

            // 5. Nén chất lượng (JPEG 70% là mức cân bằng tốt nhất)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();

            // Dọn dẹp bộ nhớ ngay lập tức
            if (sampledBitmap != finalBitmap) sampledBitmap.recycle();
            finalBitmap.recycle();

            // 6. Chuyển sang Base64 (Xóa Prefix để tránh lỗi 500 trên Server)
            String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            Log.d("IMAGE_DEBUG", "Size: " + base64String.length() + " chars (~" + (base64String.length() * 0.75 / 1024) + " KB)");

            return base64String;

        } catch (Exception e) {
            Log.e("IMAGE_ERROR", "Lỗi xử lý ảnh: " + e.getMessage());
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) return bitmap;

        float ratio = Math.min((float) MAX_DIMENSION / width, (float) MAX_DIMENSION / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}