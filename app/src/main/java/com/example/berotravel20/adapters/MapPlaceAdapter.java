package com.example.berotravel20.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.ui.main.place.PlaceFragment;
import com.example.berotravel20.ui.map.MapActivity;
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

    // [MỚI] Biến lưu layout ID để thay đổi giao diện (Ngang/Dọc)
    private int itemLayoutRes;

    public interface OnItemClickListener {
        void onFavoriteClick(Place place);
    }

    // --- CONSTRUCTOR 1: Mặc định (Dùng layout item_place_result cũ - Ngang) ---
    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.itemLayoutRes = R.layout.item_place_result; // Mặc định
    }

    // --- CONSTRUCTOR 2: Tùy chỉnh (Dùng layout truyền vào - Dọc) ---
    // [ĐÂY LÀ PHẦN SỬA LỖI CHO BẠN]
    public MapPlaceAdapter(Context context, int itemLayoutRes, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.itemLayoutRes = itemLayoutRes;
    }

    // --- DATA METHODS ---
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
        // [QUAN TRỌNG] Sử dụng itemLayoutRes thay vì fix cứng layout
        View view = LayoutInflater.from(context).inflate(itemLayoutRes, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        if (place == null) return;

        // 1. Bind Text
        if (holder.tvName != null) holder.tvName.setText(place.name);
        if (holder.tvAddress != null) holder.tvAddress.setText(place.address != null ? place.address : "Chưa có địa chỉ");
        if (holder.tvCategory != null) holder.tvCategory.setText(CategoryUtils.getLabel(place.category));

        // 2. Bind Image
        if (holder.imgPlace != null) {
            Glide.with(context)
                    .load(place.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.imgPlace);
        }

        // 3. Bind Status Info
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

        // 4. Bind Favorite
        boolean isFav = favoriteIds.contains(place.id);
        if (holder.btnFavorite != null) {
            holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
            holder.btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(place);
            });
        }

        // --- NAVIGATION LOGIC ---

        // Click toàn bộ item -> Mở chi tiết
        holder.itemView.setOnClickListener(v -> navigateToDetail(place));

        // Click nút Chi tiết -> Mở chi tiết
        if (holder.btnDetail != null) {
            holder.btnDetail.setOnClickListener(v -> navigateToDetail(place));
        }

        // Click nút Chỉ đường -> Mở MapActivity
        if (holder.btnDirection != null) {
            holder.btnDirection.setOnClickListener(v -> {
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("ACTION_TYPE", "DIRECT_TO_PLACE");
                intent.putExtra("TARGET_LAT", place.latitude);
                intent.putExtra("TARGET_LNG", place.longitude);
                intent.putExtra("TARGET_NAME", place.name);
                context.startActivity(intent);
            });
        }
    }

    private void navigateToDetail(Place place) {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            activity.getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.base_container, PlaceFragment.newInstance(place.id))
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(context, "Không thể mở chi tiết", Toast.LENGTH_SHORT).show();
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