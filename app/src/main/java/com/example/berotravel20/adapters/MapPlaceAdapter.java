package com.example.berotravel20.adapters;

import android.content.Context;
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
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.utils.CategoryUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapPlaceAdapter extends RecyclerView.Adapter<MapPlaceAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> placeList = new ArrayList<>();
    private Set<String> favoriteIds = new HashSet<>();
    private OnItemClickListener listener;
    private int itemLayoutRes;

    // Interface được mở rộng để Host xử lý logic điều hướng
    public interface OnItemClickListener {
        void onFavoriteClick(Place place);
        void onItemClick(Place place);      // Để mở chi tiết (PlaceFragment)
        void onDirectionClick(Place place); // Để mở bản đồ (MapActivity)
    }

    // Constructor 1: Dùng layout mặc định (Ngang)
    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.itemLayoutRes = R.layout.item_place_result;
    }

    // Constructor 2: Truyền layout tùy chỉnh (Ví dụ: Thẻ dọc item_place_vertical)
    public MapPlaceAdapter(Context context, int itemLayoutRes, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.itemLayoutRes = itemLayoutRes;
    }

    // --- Cập nhật dữ liệu ---
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

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(itemLayoutRes, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        if (place == null) return;

        // 1. Bind Thông tin văn bản (Check null để an toàn cho nhiều loại layout)
        if (holder.tvName != null) holder.tvName.setText(place.name);
        if (holder.tvAddress != null) holder.tvAddress.setText(place.address != null ? place.address : "Chưa có địa chỉ");
        if (holder.tvCategory != null) holder.tvCategory.setText(CategoryUtils.getLabel(place.category));

        // 2. Bind Hình ảnh
        if (holder.imgPlace != null) {
            Glide.with(context)
                    .load(place.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.imgPlace);
        }

        // 3. Bind Trạng thái & Khoảng cách
        if (holder.tvStatusInfo != null) {
            StringBuilder sb = new StringBuilder();
            if (place.status != null) {
                sb.append("open".equalsIgnoreCase(place.status.initialStatus) ? "Đang mở" : "Đóng cửa");
                if (place.status.price > 0) {
                    sb.append(" • ").append(String.format("%,.0f đ", place.status.price));
                }
            }
            if (place.distance != null) {
                sb.append(" • ").append(String.format("%.1f km", place.distance));
            }
            holder.tvStatusInfo.setText(sb.toString().isEmpty() ? "Thông tin đang cập nhật" : sb.toString());
        }

        // 4. Bind Nút Yêu thích
        boolean isFav = favoriteIds.contains(place.id);
        if (holder.btnFavorite != null) {
            holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
            holder.btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(place);
            });
        }

        // --- XỬ LÝ SỰ KIỆN CLICK (Gửi ngược về Fragment/Activity) ---

        // Click vào toàn bộ thẻ hoặc nút chi tiết -> Gọi onItemClick
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(place);
        });

        if (holder.btnDetail != null) {
            holder.btnDetail.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(place);
            });
        }

        // Click nút Chỉ đường -> Gọi onDirectionClick
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
        ImageView imgPlace, btnFavorite;
        TextView tvName, tvAddress, tvStatusInfo, tvCategory;
        View btnDirection;
        Button btnDetail;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatusInfo = itemView.findViewById(R.id.tvStatusInfo);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnDirection = itemView.findViewById(R.id.btnDirections);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}