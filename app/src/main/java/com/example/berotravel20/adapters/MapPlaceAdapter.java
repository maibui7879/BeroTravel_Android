package com.example.berotravel20.adapters;

import android.content.Context;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class MapPlaceAdapter extends RecyclerView.Adapter<MapPlaceAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> placeList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place place);
    }

    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<Place> list) {
        this.placeList.clear();
        if (list != null) {
            this.placeList.addAll(list);
        }
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

        // 1. Tên & Địa chỉ
        if (holder.tvName != null) holder.tvName.setText(place.name);
        if (holder.tvAddress != null) holder.tvAddress.setText(place.address);

        // 2. Ảnh
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

        // 3. LOGIC HIỂN THỊ THÔNG TIN CHI TIẾT
        StringBuilder statusBuilder = new StringBuilder();

        // A. Trạng thái (Mở/Đóng)
        if (place.status != null) {
            if ("open".equalsIgnoreCase(place.status.initialStatus)) {
                statusBuilder.append("Đang mở");
            } else {
                statusBuilder.append("Đóng cửa");
            }

            // B. Giá
            if (place.status.price > 0) {
                statusBuilder.append(" • VND").append(String.format("%.0f", place.status.price));
            } else {
                statusBuilder.append(" • Miễn phí");
            }
        } else {
            statusBuilder.append("Thông tin");
        }

        // C. KHOẢNG CÁCH (Đây là phần bạn cần)
        // Log kiểm tra xem dữ liệu có vào không
        if (place.distance != null) {
            Log.d("ADAPTER", "Item: " + place.name + " - Dist: " + place.distance);

            // Format: " • Cách 1.2 km"
            statusBuilder.append(" • Cách ")
                    .append(String.format("%.1f", place.distance))
                    .append(" km");
        }

        // Gán text
        if (holder.tvStatusInfo != null) {
            holder.tvStatusInfo.setText(statusBuilder.toString());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(place);
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvName, tvAddress, tvStatusInfo;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatusInfo = itemView.findViewById(R.id.tvStatusInfo);
        }
    }
}