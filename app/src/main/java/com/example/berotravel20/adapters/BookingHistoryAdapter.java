package com.example.berotravel20.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Booking.Booking;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {
    private List<Booking> bookingList;
    private OnBookingActionListener listener;

    // Interface xử lý sự kiện
    public interface OnBookingActionListener {
        void onPay(Booking booking);
        void onCancel(Booking booking);
        void onItemClick(Booking booking);
    }

    public BookingHistoryAdapter(List<Booking> bookingList, OnBookingActionListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    public void updateList(List<Booking> newList) {
        this.bookingList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        if (booking == null) return;

        // 1. Hiển thị thông tin
        if (booking.place != null) {
            holder.tvPlaceName.setText(booking.place.name);
            Glide.with(holder.itemView.getContext())
                    .load(booking.place.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.imgPlace);
        }

        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", booking.totalPrice));

        // Cắt chuỗi ngày ISO (yyyy-MM-dd)
        if (booking.bookingDateTime != null && booking.bookingDateTime.length() >= 10) {
            holder.tvDate.setText(booking.bookingDateTime.substring(0, 10));
        }

        // 2. Xử lý trạng thái và ẩn hiện nút hành động
        if (booking.isPaid) {
            holder.tvStatus.setText("Đã thanh toán");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_success);
            holder.layoutActions.setVisibility(View.GONE); // Đã trả tiền thì không hiện nút Pay/Cancel
        } else {
            holder.tvStatus.setText("Chờ thanh toán");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.layoutActions.setVisibility(View.VISIBLE);
        }

        // 3. Sự kiện Click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(booking));
        holder.btnPay.setOnClickListener(v -> listener.onPay(booking));
        holder.btnCancel.setOnClickListener(v -> listener.onCancel(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceName, tvDate, tvPrice, tvStatus;
        ImageView imgPlace;
        Button btnPay, btnCancel;
        View layoutActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name_history);
            tvDate = itemView.findViewById(R.id.tv_booking_date_history);
            tvPrice = itemView.findViewById(R.id.tv_total_price_history);
            tvStatus = itemView.findViewById(R.id.tv_status_history);
            imgPlace = itemView.findViewById(R.id.img_place_history);
            btnPay = itemView.findViewById(R.id.btn_pay_now);
            btnCancel = itemView.findViewById(R.id.btn_cancel_booking);
            layoutActions = itemView.findViewById(R.id.layout_actions); // Cần thêm ID này vào XML
        }
    }
}