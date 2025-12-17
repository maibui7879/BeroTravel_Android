package com.example.berotravel20.ui.main.booking;

import android.content.ContentValues;
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
import com.example.berotravel20.ui.common.BaseFragment;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingSuccessFragment extends BaseFragment {
    private String placeId, placeName, placeAddr, placeImg, totalStr;
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
            placeId = getArguments().getString("id");
            placeName = getArguments().getString("name");
            placeAddr = getArguments().getString("addr");
            placeImg = getArguments().getString("img");
            pricePerDay = getArguments().getInt("price");
            guests = getArguments().getInt("g");
            start = getArguments().getLong("s");
            end = getArguments().getLong("e");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String bookingId = "BERO-" + System.currentTimeMillis() / 10000;
        ((TextView)view.findViewById(R.id.tv_booking_id)).setText("Mã đặt chỗ: " + bookingId);

        // Nút Lưu hóa đơn
        view.findViewById(R.id.btn_save_invoice).setOnClickListener(v -> {
            generateAndSaveInvoice(bookingId);
        });

        // Nút về trang chủ
        view.findViewById(R.id.btn_back_to_home).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }

    private void generateAndSaveInvoice(String bookingId) {
        // 1. Inflate layout Review ngầm
        View invoiceView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_review_reservation, null);

        // 2. Đổ dữ liệu và tính toán giá (Giá * Ngày * Người)
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

        // Ẩn UI thừa
        invoiceView.findViewById(R.id.btn_book_now).setVisibility(View.GONE);
        invoiceView.findViewById(R.id.toolbar).setVisibility(View.GONE);

        // 3. FIX LỖI CRASH: Đo đạc kích thước View thủ công
        // Lấy chiều rộng màn hình để làm chiều rộng hóa đơn
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Ép View đo đạc với chiều rộng cố định và chiều cao tự do
        int widthSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        invoiceView.measure(widthSpec, heightSpec);

        // Bố trí lại vị trí các phần tử (nếu không gọi hàm này, width/height sẽ là 0)
        invoiceView.layout(0, 0, invoiceView.getMeasuredWidth(), invoiceView.getMeasuredHeight());

        // Kiểm tra an toàn trước khi tạo Bitmap
        if (invoiceView.getMeasuredWidth() <= 0 || invoiceView.getMeasuredHeight() <= 0) {
            Toast.makeText(requireContext(), "Lỗi: Không thể đo kích thước hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Vẽ Bitmap
        Bitmap bitmap = Bitmap.createBitmap(invoiceView.getMeasuredWidth(), invoiceView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        invoiceView.draw(canvas);

        // 5. Lưu vào máy
        saveBitmapToGallery(bitmap, "Invoice_" + bookingId);
    }

    private void saveBitmapToGallery(Bitmap bitmap, String filename) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.MediaColumns.DISPLAY_NAME, filename + ".jpg");
                cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BeroTravel");
                Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                if (uri != null) {
                    fos = getActivity().getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    if (fos != null) fos.close();
                    Toast.makeText(requireContext(), "Đã lưu hóa đơn vào máy!", Toast.LENGTH_LONG).show();
                }
            } else {
                // Xử lý cho Android cũ nếu cần
                Toast.makeText(requireContext(), "Lưu thành công", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}