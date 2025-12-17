package com.example.berotravel20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.utils.CategoryUtils; // Import Utils để lấy tên thể loại

import java.util.ArrayList;
import java.util.List;

public class MapPlaceAdapter extends RecyclerView.Adapter<MapPlaceAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> placeList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place place);
        void onDirectionClick(Place place);
    }

    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // 1. Dùng cho trang 1 (Xóa cũ, thêm mới)
    public void setData(List<Place> list) {
        this.placeList.clear();
        if (list != null) {
            this.placeList.addAll(list);
        }
        notifyDataSetChanged();
    }

    // 2. Dùng cho trang 2, 3... (Nối thêm vào đuôi)
    public void addData(List<Place> newPlaces) {
        if (newPlaces != null && !newPlaces.isEmpty()) {
            int startPos = this.placeList.size();
            this.placeList.addAll(newPlaces);
            notifyItemRangeInserted(startPos, newPlaces.size());
        }
    }

    // 3. Xóa sạch dữ liệu
    public void clearData() {
        this.placeList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo tên layout đúng với file XML bạn đang dùng (item_place_result hoặc item_map_place)
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_result, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);

        // 1. Gán Tên & Địa chỉ
        if (holder.tvName != null) holder.tvName.setText(place.name);
        if (holder.tvAddress != null) holder.tvAddress.setText(place.address);

        // 2. [MỚI] Gán Category (Thể loại)
        if (holder.tvCategory != null) {
            String categoryLabel = CategoryUtils.getLabel(place.category);
            holder.tvCategory.setText(categoryLabel);
        }

        // 3. Load Ảnh bằng Glide
        if (holder.imgPlace != null) {
            // Logic: Kiểm tra imageUrl
            if (place.imageUrl != null && !place.imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(place.imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(holder.imgPlace);
            } else {
                // Nếu không có link ảnh thì set ảnh mặc định
                holder.imgPlace.setImageResource(R.drawable.placeholder_image);
            }
        }

        // 4. Xây dựng chuỗi thông tin (Trạng thái • Giá • Khoảng cách)
        StringBuilder statusBuilder = new StringBuilder();
        if (place.status != null) {
            if ("open".equalsIgnoreCase(place.status.initialStatus)) {
                statusBuilder.append("Đang mở");
            } else {
                statusBuilder.append("Đóng cửa");
            }
            if (place.status.price > 0) {
                // Format giá tiền có dấu phẩy (VD: 50,000 đ)
                statusBuilder.append(" • ").append(String.format("%,.0f đ", place.status.price));
            } else {
                statusBuilder.append(" • Miễn phí");
            }
        } else {
            statusBuilder.append("Thông tin");
        }

        if (place.distance != null) {
            statusBuilder.append(" • Cách ").append(String.format("%.1f km", place.distance));
        }

        if (holder.tvStatusInfo != null) {
            holder.tvStatusInfo.setText(statusBuilder.toString());
        }

        // 5. Sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(place);
        });

        if (holder.btnDirection != null) {
            holder.btnDirection.setOnClickListener(v -> {
                if (listener != null) listener.onDirectionClick(place);
            });
        }
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvName, tvAddress, tvStatusInfo, tvCategory; // [MỚI] Thêm tvCategory
        View btnDirection;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ View từ XML
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatusInfo = itemView.findViewById(R.id.tvStatusInfo);
            btnDirection = itemView.findViewById(R.id.btnDirections);

            // [MỚI] Ánh xạ Category (Quan trọng: ID phải khớp với XML)
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}