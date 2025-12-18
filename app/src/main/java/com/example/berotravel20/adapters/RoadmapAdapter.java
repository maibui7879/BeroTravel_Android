package com.example.berotravel20.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Journey.Journey;

import java.util.List;

public class RoadmapAdapter extends RecyclerView.Adapter<RoadmapAdapter.ViewHolder> {
    private List<Journey.JourneyPlace> places;

    // --- 1. KHAI BÁO INTERFACE XÓA ---
    public interface OnDeleteListener {
        void onDelete(Journey.JourneyPlace item);
    }
    private OnDeleteListener deleteListener;

    public RoadmapAdapter(List<Journey.JourneyPlace> places) {
        this.places = places;
    }

    // --- 2. SETTER CHO FRAGMENT GỌI ---
    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roadmap_place, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Journey.JourneyPlace item = places.get(position);
        Journey.PlaceDetail detail = item.place;

        int colorDone = ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_700);
        int colorPending = ContextCompat.getColor(holder.itemView.getContext(), R.color.gray_400);

        // --- Logic Timeline (Giữ nguyên của bạn) ---
        holder.lineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.lineBottom.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);

        holder.lineTop.setBackgroundColor(item.visited ? colorDone : colorPending);
        holder.lineBottom.setBackgroundColor(item.visited ? colorDone : colorPending);

        // Fix lỗi hiển thị icon dot/check
        if (item.visited) {
            holder.imgDot.setImageResource(R.drawable.ic_check_circle);
            holder.imgDot.setColorFilter(colorDone);

            holder.tvStatus.setText("ĐÃ GHÉ THĂM");
            holder.tvStatus.setTextColor(colorDone);
            holder.cardPlace.setAlpha(0.7f); // Mờ đi chút

            // Đã đi rồi thì KHÔNG cho xóa (logic nghiệp vụ)
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.imgDot.setImageResource(R.drawable.ic_dot_circle);
            holder.imgDot.setColorFilter(colorPending);

            holder.tvStatus.setText("SẮP ĐẾN");
            holder.tvStatus.setTextColor(colorPending);
            holder.cardPlace.setAlpha(1.0f);

            // Chưa đi thì HIỆN nút xóa
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        // --- Nạp dữ liệu ---
        if (detail != null) {
            holder.tvName.setText(detail.name);
            holder.tvAddress.setText(detail.address);

            Glide.with(holder.itemView.getContext())
                    .load(detail.imageUrl)
                    .placeholder(R.drawable.placeholder_image) // Nhớ tạo ảnh này hoặc xóa dòng này
                    .into(holder.ivPlace);
        } else {
            holder.tvName.setText("Địa điểm lỗi");
            holder.tvAddress.setText("Không có dữ liệu");
        }

        // --- 3. BẮT SỰ KIỆN XÓA ---
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvStatus;
        View lineTop, lineBottom;
        ImageView imgDot, ivPlace;
        CardView cardPlace;
        ImageView btnDelete; // Thêm nút xóa

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_place_name_roadmap);
            tvAddress = v.findViewById(R.id.tv_place_address_roadmap);
            tvStatus = v.findViewById(R.id.tv_status_visited);
            lineTop = v.findViewById(R.id.line_top);
            lineBottom = v.findViewById(R.id.line_bottom);
            imgDot = v.findViewById(R.id.img_dot);
            ivPlace = v.findViewById(R.id.iv_place_roadmap);
            cardPlace = v.findViewById(R.id.card_place);

            // Ánh xạ nút xóa mới
            btnDelete = v.findViewById(R.id.btn_delete_place);
        }
    }
}