package com.example.berotravel20.ui.main.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.models.PlaceResponse;

import java.util.List;

public class HomePlaceAdapter extends RecyclerView.Adapter<HomePlaceAdapter.PlaceViewHolder> {

    private List<PlaceResponse.Place> places;
    private OnPlaceClickListener listener;

    public interface OnPlaceClickListener {
        void onPlaceClick(PlaceResponse.Place place);
    }

    public HomePlaceAdapter(List<PlaceResponse.Place> places, OnPlaceClickListener listener) {
        this.places = places;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        PlaceResponse.Place place = places.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvName, tvAddress, tvPrice, tvCategory;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.img_place);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvCategory = itemView.findViewById(R.id.tv_category);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onPlaceClick(places.get(getAdapterPosition()));
                }
            });
        }

        void bind(PlaceResponse.Place place) {
            tvName.setText(place.name);
            tvAddress.setText(place.address);
            if (place.category != null && !place.category.isEmpty()) {
                tvCategory.setText(place.category);
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            double price = place.price;
            // Check status price override logic if needed, similar to PlaceFragment
            if (place.status != null && place.status.price > 0) {
                price = place.status.price;
            }

            if (price == 0) {
                tvPrice.setText("Free");
            } else {
                tvPrice.setText(String.format("$%,.0f", price));
            }

            if (place.image_url != null && !place.image_url.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(place.image_url)
                        .placeholder(R.color.teal_200)
                        .centerCrop()
                        .into(imgPlace);
            }
        }
    }
}
