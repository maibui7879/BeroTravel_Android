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

import java.util.ArrayList;
import java.util.List;

public class MapPlaceAdapter extends RecyclerView.Adapter<MapPlaceAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> placeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place place);
    }

    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.placeList = new ArrayList<>();
    }

    public void setData(List<Place> list) {
        this.placeList = list;
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

        // 1. Bind thông tin cơ bản
        holder.tvName.setText(place.name);
        holder.tvAddress.setText(place.address);

        // 2. Bind ảnh dùng Glide
        if (place.imageUrl != null) {
            Glide.with(context).load(place.imageUrl).into(holder.imgPlace);
        }

        // 3. LOGIC PlaceStatus (Quan trọng)
        if (place.status != null) {
            // Hiển thị trạng thái (Open/Closed) hoặc Giá
            String statusText = "";
            if ("open".equalsIgnoreCase(place.status.initialStatus)) {
                statusText = "Đang mở cửa • ";
            } else {
                statusText = "Đóng cửa • ";
            }

            // Hiển thị giá (nếu > 0)
            if (place.status.price > 0) {
                statusText += String.format("$%.0f", place.status.price);
            } else {
                statusText += "Miễn phí";
            }

            // Tạm thời gán vào chỗ rating hoặc tạo textview mới
            holder.tvStatusInfo.setText(statusText);
        }

        // Sự kiện click vào item -> Di chuyển map tới đó
        holder.itemView.setOnClickListener(v -> listener.onItemClick(place));
    }

    @Override
    public int getItemCount() {
        return placeList != null ? placeList.size() : 0;
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvName, tvAddress, tvStatusInfo;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            // Tận dụng TextView rating trong layout cũ để hiển thị status
            tvStatusInfo = itemView.findViewById(R.id.layoutRating).findViewById(R.id.tvPlaceName); // Sửa ID trong layout xml nếu cần chuẩn hơn
        }
    }
}