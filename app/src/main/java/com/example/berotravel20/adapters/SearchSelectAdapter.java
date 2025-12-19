package com.example.berotravel20.adapters;

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

public class SearchSelectAdapter extends RecyclerView.Adapter<SearchSelectAdapter.ViewHolder> {

    private List<Place> list = new ArrayList<>();
    private OnPlaceClickListener listener;

    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }

    public SearchSelectAdapter(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Place> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_place_selectable, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place p = list.get(position);
        holder.name.setText(p.name);
        holder.address.setText(p.address != null ? p.address : "Chưa cập nhật địa chỉ");

        Glide.with(holder.itemView.getContext())
                .load(p.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.img);

        holder.itemView.setOnClickListener(v -> listener.onPlaceClick(p));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;
        ImageView img;
        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvPlaceName);
            address = v.findViewById(R.id.tvAddress);
            img = v.findViewById(R.id.imgPlace);
        }
    }
}