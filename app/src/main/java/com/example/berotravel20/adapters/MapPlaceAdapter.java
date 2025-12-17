package com.example.berotravel20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // [QUAN TRỌNG]
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

    // Set chứa ID các địa điểm user đã yêu thích
    private Set<String> favoriteIds = new HashSet<>();

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place place);       // Click vào item -> Move camera
        void onDirectionClick(Place place);  // Click chỉ đường -> Vẽ đường
        void onFavoriteClick(Place place);   // Click tim -> Gọi API Toggle
        void onDetailClick(Place place);     // [MỚI] Click chi tiết -> Mở PlaceFragment
    }

    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // --- CẬP NHẬT DỮ LIỆU ---
    public void setFavoriteIds(List<String> ids) {
        this.favoriteIds.clear();
        if (ids != null) {
            this.favoriteIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    public void toggleFavoriteLocal(String placeId) {
        if (favoriteIds.contains(placeId)) {
            favoriteIds.remove(placeId);
        } else {
            favoriteIds.add(placeId);
        }
        notifyDataSetChanged();
    }

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

        // 1. Bind Data
        if (holder.tvName != null) holder.tvName.setText(place.name);
        if (holder.tvAddress != null) holder.tvAddress.setText(place.address != null ? place.address : "Chưa có địa chỉ");
        if (holder.tvCategory != null) holder.tvCategory.setText(CategoryUtils.getLabel(place.category));

        // 2. Load Image
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

        // 4. Favorite Logic
        boolean isFav = favoriteIds.contains(place.id);
        holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart);

        // 5. Click Events
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(place);
        });

        if (holder.btnDirection != null) {
            holder.btnDirection.setOnClickListener(v -> {
                if (listener != null) listener.onDirectionClick(place);
            });
        }

        if (holder.btnFavorite != null) {
            holder.btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(place);
            });
        }

        // [MỚI] Click Detail
        if (holder.btnDetail != null) {
            holder.btnDetail.setOnClickListener(v -> {
                if (listener != null) listener.onDetailClick(place);
            });
        }
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace, btnFavorite;
        TextView tvName, tvAddress, tvStatusInfo, tvCategory;
        View btnDirection;
        Button btnDetail; // [MỚI] Nút Chi tiết

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatusInfo = itemView.findViewById(R.id.tvStatusInfo);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnDirection = itemView.findViewById(R.id.btnDirections);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            // [MỚI] Ánh xạ btnDetail (ID phải trùng với XML item_place_result)
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}