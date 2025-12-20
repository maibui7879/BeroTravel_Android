package com.example.berotravel20.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import androidx.exifinterface.media.ExifInterface;
import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ImageUtils {
    private static final String TAG = "IMAGE_UTILS";

    // Thay bằng thông tin của bạn
    private static final String CLOUD_NAME = "dkshpgp3n";
    private static final String UPLOAD_PRESET = "ml_default"; // Bắt buộc phải là Unsigned Preset
    private static final int MAX_DIMENSION = 1024;

    // Hàm khởi tạo để trống để tương thích code cũ (không cần SDK nữa)
    public static void initCloudinary() { }

    /**
     * GIỮ NGUYÊN TÊN HÀM: uriToBase64
     * LOGIC MỚI: Dùng OkHttp upload file trực tiếp
     */
    public static void uriToBase64(Context context, Uri uri, CloudinaryCallback callback) {
        new Thread(() -> {
            try {
                // 1. Nén và xoay ảnh về đúng chiều
                Bitmap finalBitmap = getProcessedBitmap(context, uri);

                // 2. Lưu ra file tạm trong cache
                File tempFile = new File(context.getCacheDir(), "upload_temp_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fos = new FileOutputStream(tempFile);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos); // Nén JPEG 80%
                fos.close();

                // 3. Cấu hình OkHttp
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();

                // 4. Tạo Multipart Body (File + Preset)
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", tempFile.getName(),
                                RequestBody.create(tempFile, MediaType.parse("image/jpeg")))
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .build();

                // 5. Tạo Request gửi đến API Cloudinary
                Request request = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload")
                        .post(requestBody)
                        .build();

                // 6. Thực thi
                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    // Parse JSON lấy link ảnh
                    JSONObject json = new JSONObject(responseBody);
                    String secureUrl = json.getString("secure_url");
                    callback.onSuccess(secureUrl);
                } else {
                    Log.e(TAG, "Upload thất bại: " + response.code() + " - " + responseBody);
                    callback.onError("Lỗi Server: " + response.code());
                }

                // Dọn dẹp bộ nhớ
                finalBitmap.recycle();
                if (tempFile.exists()) tempFile.delete();

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // --- Các hàm xử lý Bitmap (Xoay, Resize) ---
    private static Bitmap getProcessedBitmap(Context context, Uri uri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(uri);
        Bitmap sampled = BitmapFactory.decodeStream(is);
        is.close();
        if (sampled == null) throw new IOException("Không thể đọc ảnh");

        // Xoay ảnh theo EXIF
        int rotation = 0;
        try (InputStream exifIs = context.getContentResolver().openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(exifIs);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotation = 90;
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotation = 180;
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotation = 270;
        }

        Matrix matrix = new Matrix();
        if (rotation != 0) matrix.postRotate(rotation);

        // Resize
        float ratio = Math.min((float) MAX_DIMENSION / sampled.getWidth(), (float) MAX_DIMENSION / sampled.getHeight());
        if (ratio < 1.0f) {
            matrix.postScale(ratio, ratio);
        }

        Bitmap result = Bitmap.createBitmap(sampled, 0, 0, sampled.getWidth(), sampled.getHeight(), matrix, true);
        if (sampled != result) sampled.recycle();
        return result;
    }

    public static String getOptimizedUrl(String rawUrl) {
        if (rawUrl == null || !rawUrl.contains("cloudinary.com")) return rawUrl;
        return rawUrl.replace("/upload/", "/upload/w_800,c_fill,g_auto,f_auto,q_auto/");
    }

    public interface CloudinaryCallback {
        void onSuccess(String url);
        void onError(String error);
    }
}