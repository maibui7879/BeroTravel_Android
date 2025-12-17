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
import com.example.berotravel20.utils.CategoryUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapPlaceAdapter extends RecyclerView.Adapter<MapPlaceAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> placeList = new ArrayList<>();

    // Set chứa ID các địa điểm user đã yêu thích (Dùng Set để tìm kiếm nhanh)
    private Set<String> favoriteIds = new HashSet<>();

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place place);       // Click vào item -> Move camera
        void onDirectionClick(Place place);  // Click chỉ đường -> Vẽ đường
        void onFavoriteClick(Place place);   // Click tim -> Gọi API Toggle
    }

    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách ID yêu thích.
     * Được gọi từ Activity sau khi fetch API thành công.
     */
    public void setFavoriteIds(List<String> ids) {
        this.favoriteIds.clear();
        if (ids != null) {
            this.favoriteIds.addAll(ids);
        }
        notifyDataSetChanged(); // Refresh lại toàn bộ để cập nhật icon tim
    }

    /**
     * Toggle cục bộ trạng thái tim (dùng để update UI ngay khi API báo thành công)
     */
    public void toggleFavoriteLocal(String placeId) {
        if (favoriteIds.contains(placeId)) {
            favoriteIds.remove(placeId);
        } else {
            favoriteIds.add(placeId);
        }
        notifyDataSetChanged();
    }

    // --- CÁC HÀM QUẢN LÝ DỮ LIỆU LIST ---
    public void setData(List<Place> list) {
        this.placeList.clear();
        if (list != null) {
            this.placeList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addData(List<Place> newPlaces) {
        if (newPlaces != null && !newPlaces.isEmpty()) {
            int startPos = this.placeList.size();
            this.placeList.addAll(newPlaces);
            notifyItemRangeInserted(startPos, newPlaces.size());
        }
    }

    public void clearData() {
        this.placeList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_result, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);

        // 1. Gán thông tin cơ bản
        if (holder.tvName != null) holder.tvName.setText(place.name);
        if (holder.tvAddress != null) holder.tvAddress.setText(place.address != null ? place.address : "Chưa có địa chỉ");

        if (holder.tvCategory != null) {
            holder.tvCategory.setText(CategoryUtils.getLabel(place.category));
        }

        // 2. Load ảnh
        if (holder.imgPlace != null) {
            if (place.imageUrl != null && !place.imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(place.imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(holder.imgPlace);
            } else {
                holder.imgPlace.setImageResource(R.drawable.placeholder_image);
            }
        }

        // 3. Status Info
        StringBuilder statusBuilder = new StringBuilder();
        if (place.status != null) {
            statusBuilder.append("open".equalsIgnoreCase(place.status.initialStatus) ? "Đang mở" : "Đóng cửa");
            if (place.status.price > 0) {
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
        if (holder.tvStatusInfo != null) holder.tvStatusInfo.setText(statusBuilder.toString());

        // 4. [LOGIC TIM] Kiểm tra ID trong favoriteIds
        // place.id là ID đã map từ "_id"
        boolean isFav = favoriteIds.contains(place.id);
        if (isFav) {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);
            // Có thể thêm tint màu đỏ nếu icon là vector trắng
            // holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart);
            // holder.btnFavorite.clearColorFilter();
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

        // Sự kiện bấm tim
        if (holder.btnFavorite != null) {
            holder.btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(place);
            });
        }
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace, btnFavorite; // btnFavorite là ImageView (trái tim)
        TextView tvName, tvAddress, tvStatusInfo, tvCategory;
        View btnDirection; // Nút chỉ đường

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatusInfo = itemView.findViewById(R.id.tvStatusInfo);
            tvCategory = itemView.findViewById(R.id.tvCategory);

            btnDirection = itemView.findViewById(R.id.btnDirections); // ID khớp XML
            btnFavorite = itemView.findViewById(R.id.btnFavorite);     // ID khớp XML
        }
    }
}