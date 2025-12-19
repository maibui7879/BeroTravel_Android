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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Place.Place;
// import com.example.berotravel20.ui.main.place.PlaceDetailActivity; // Bỏ comment khi có Activity chi tiết

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private List<Place> places = new ArrayList<>();
    private Context context;

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

        // 1. Tên địa điểm
        holder.tvName.setText(place.name);

        // 2. Địa chỉ
        if (place.address != null) {
            holder.tvAddress.setText(place.address);
        } else {
            holder.tvAddress.setText("Chưa cập nhật địa chỉ");
        }

        // 3. Status/Rating (Tạm thời hiện category hoặc giá tiền)
        String info = "";
        if (place.category != null) info += place.category;
        //if (place.price > 0) info += " - " + String.format("%,.0f đ", place.price);
        holder.tvStatusInfo.setText(info.isEmpty() ? "Đang cập nhật" : info);

        // 4. Load Ảnh (ID là imgPlace)
        if (place.imageUrl != null && !place.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(place.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_image);
        }

        // 5. Nút Tim (Vì đây là màn hình Yêu thích nên luôn fill màu)
        // (Nếu muốn bấm vào để bỏ yêu thích thì cần xử lý sự kiện ở đây)
        holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);

        // 6. Sự kiện Click Chi tiết
        holder.btnDetail.setOnClickListener(v -> {
            Toast.makeText(context, "Mở chi tiết: " + place.name, Toast.LENGTH_SHORT).show();
            // Code mở màn hình chi tiết:
            // Intent intent = new Intent(context, PlaceDetailActivity.class);
            // intent.putExtra("place_data", place); // Place cần implements Serializable
            // context.startActivity(intent);
        });

        // 7. Sự kiện Click Chỉ đường
        holder.btnDirections.setOnClickListener(v -> {
            Toast.makeText(context, "Chỉ đường tới: " + place.name, Toast.LENGTH_SHORT).show();
            // Xử lý mở bản đồ sau
        });
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnFavorite, btnDirections;
        TextView tvName, tvAddress, tvStatusInfo;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.imgPlace);      // <-- ID chuẩn
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);   // <-- ID chuẩn
            tvStatusInfo = itemView.findViewById(R.id.tvStatusInfo);

            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnDirections = itemView.findViewById(R.id.btnDirections);
        }
    }
}