package com.example.berotravel20.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private List<String> favoriteIds = new ArrayList<>();

    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng tạm layout item_place_result nếu có, hoặc tạo một layout đơn giản
        // Ở đây mình dùng layout có sẵn android.R.layout.simple_list_item_1 cho nhanh
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String placeId = favoriteIds.get(position);
        // Vì chưa có API lấy chi tiết Place từ ID, mình hiển thị tạm ID
        holder.tvName.setText("Địa điểm ID: " + placeId);
    }

    @Override
    public int getItemCount() {
        return favoriteIds != null ? favoriteIds.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(android.R.id.text1);
        }
    }
}