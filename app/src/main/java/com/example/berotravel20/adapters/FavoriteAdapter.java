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

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private List<Place> places = new ArrayList<>();
    private Context context;

    // 1. Khai báo Interface lắng nghe sự kiện
    private OnFavoriteActionListener listener;

    public interface OnFavoriteActionListener {
        void onPlaceClick(Place place);
        void onDirectionClick(Place place);
    }

    // 2. Hàm set Listener
    public void setListener(OnFavoriteActionListener listener) {
        this.listener = listener;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = places.get(position);

        holder.tvName.setText(place.name);
        holder.tvAddress.setText(place.address != null ? place.address : "Chưa cập nhật địa chỉ");

        String info = "";
        if (place.category != null) info += place.category;
        holder.tvStatusInfo.setText(info.isEmpty() ? "Đang cập nhật" : info);

        if (place.imageUrl != null && !place.imageUrl.isEmpty()) {
            Glide.with(context).load(place.imageUrl).placeholder(R.drawable.placeholder_image).centerCrop().into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);

        // 3. Gọi Listener khi click Chi tiết
        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) listener.onPlaceClick(place);
        });

        // 4. Gọi Listener khi click Chỉ đường
        holder.btnDirections.setOnClickListener(v -> {
            if (listener != null) listener.onDirectionClick(place);
        });
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnFavorite, btnDirections; // Sửa btnDirections thành View nếu trong XML nó không phải ImageView
        TextView tvName, tvAddress, tvStatusInfo;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnDirections = itemView.findViewById(R.id.btnDirections);
        }
    }
}