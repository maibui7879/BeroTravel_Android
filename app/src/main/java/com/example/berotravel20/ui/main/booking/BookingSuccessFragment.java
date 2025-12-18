package com.example.berotravel20.ui.main.booking;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.berotravel20.R;
import com.example.berotravel20.ui.common.BaseActivity;
import com.example.berotravel20.ui.common.BaseFragment;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingSuccessFragment extends BaseFragment {
    private String placeId, placeName, placeAddr, placeImg;
    private int guests, pricePerDay;
    private long start, end;

    public static BookingSuccessFragment newInstance(String id, String name, String addr, String img, int price, int g, long start, long end) {
        BookingSuccessFragment f = new BookingSuccessFragment();
        Bundle b = new Bundle();
        b.putString("id", id); b.putString("name", name); b.putString("addr", addr);
        b.putString("img", img); b.putInt("price", price); b.putInt("g", g);
        b.putLong("s", start); b.putLong("e", end);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString("id"); placeName = getArguments().getString("name");
            placeAddr = getArguments().getString("addr"); placeImg = getArguments().getString("img");
            pricePerDay = getArguments().getInt("price"); guests = getArguments().getInt("g");
            start = getArguments().getLong("s"); end = getArguments().getLong("e");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tạo mã đặt chỗ ngẫu nhiên
        String bookingId = "BERO-" + (System.currentTimeMillis() / 100000);
        ((TextView)view.findViewById(R.id.tv_booking_id)).setText("Mã đặt chỗ: " + bookingId);

        // Hiển thị email người dùng (giả định từ TokenManager hoặc truyền vào)
        if (tokenManager.getUsername() != null) {
            ((TextView)view.findViewById(R.id.tv_user_email)).setText(tokenManager.getUsername());
        }

        // Logic lưu hóa đơn
        view.findViewById(R.id.btn_save_invoice).setOnClickListener(v -> generateAndSaveInvoice(bookingId));

        // Logic chuyển về trang chủ / Lịch sử (Review Reservation)
        view.findViewById(R.id.btn_back_to_home).setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("NAVIGATE_TO", "BOOKING_HISTORY");
                startActivity(intent);
            }
        });
    }

    // Logic xử lý xuất hóa đơn thành ảnh JPG
    private void generateAndSaveInvoice(String bookingId) {
        View invoiceView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_review_reservation, null);
        long nights = (end - start) / (24 * 60 * 60 * 1000);
        if (nights < 1) nights = 1;
        double totalInvoice = (pricePerDay * nights * guests) * 1.05;

        ((TextView) invoiceView.findViewById(R.id.tv_place_name_review)).setText(placeName);
        ((TextView) invoiceView.findViewById(R.id.tv_place_address_review)).setText(placeAddr);
        ((TextView) invoiceView.findViewById(R.id.tv_total_review)).setText(String.format("%,.0f đ", totalInvoice));
        ((TextView) invoiceView.findViewById(R.id.tv_stay_guests)).setText(guests + " khách");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        ((TextView) invoiceView.findViewById(R.id.tv_checkin_review)).setText(sdf.format(new Date(start)));
        ((TextView) invoiceView.findViewById(R.id.tv_checkout_review)).setText(sdf.format(new Date(end)));

        invoiceView.findViewById(R.id.btn_book_now).setVisibility(View.GONE);
        invoiceView.findViewById(R.id.toolbar).setVisibility(View.GONE);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int widthSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        invoiceView.measure(widthSpec, heightSpec);
        invoiceView.layout(0, 0, invoiceView.getMeasuredWidth(), invoiceView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(invoiceView.getMeasuredWidth(), invoiceView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        invoiceView.draw(canvas);
        saveBitmapToGallery(bitmap, "Invoice_" + bookingId);
    }

    private void saveBitmapToGallery(Bitmap bitmap, String filename) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.MediaColumns.DISPLAY_NAME, filename + ".jpg");
                cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BeroTravel");
                Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                if (uri != null) {
                    OutputStream fos = getActivity().getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    if (fos != null) fos.close();
                    showSuccess("Đã lưu hóa đơn vào thư viện ảnh!");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}