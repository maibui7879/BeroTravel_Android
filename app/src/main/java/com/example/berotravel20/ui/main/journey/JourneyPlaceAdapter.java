package com.example.berotravel20.ui.main.journey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;

import java.util.List;
import java.util.Map;

public class JourneyPlaceAdapter extends RecyclerView.Adapter<JourneyPlaceAdapter.ViewHolder> {

    // Using Map<String, String> to store simple place data: id, name, address,
    // image_url
    private List<Map<String, String>> places;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public JourneyPlaceAdapter(List<Map<String, String>> places, OnItemClickListener listener) {
        this.places = places;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journey_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> place = places.get(position);
        holder.tvName.setText(place.get("name"));
        holder.tvAddress.setText(place.get("address"));

        String start = place.get("startTime");
        String end = place.get("endTime");
        if (start != null && end != null) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(formatTimeRange(start, end));
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        String imageUrl = place.get("image_url");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgPlace);
        } else {
            holder.imgPlace.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    private String formatTimeRange(String start, String end) {
        try {
            java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    java.util.Locale.getDefault());
            iso.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date d1 = iso.parse(start);
            java.util.Date d2 = iso.parse(end);

            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy",
                    java.util.Locale.getDefault());
            return out.format(d1) + " - " + out.format(d2);
        } catch (Exception e) {
            return start + " - " + end;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace, btnDelete;
        TextView tvName, tvAddress, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.img_place);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            tvName = itemView.findViewById(R.id.tv_place_name);
            tvAddress = itemView.findViewById(R.id.tv_place_address);
            tvTime = itemView.findViewById(R.id.tv_place_time);
        }
    }
}
