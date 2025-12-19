package com.example.berotravel20.adapters;

import android.content.Context;
import android.graphics.Color;
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
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Place.Place;
import com.example.berotravel20.data.model.Review.Review;
import com.example.berotravel20.data.repository.ReviewRepository;
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

    public interface OnItemClickListener {
        void onFavoriteClick(Place place);
        void onItemClick(Place place);
        void onDirectionClick(Place place);
    }

    public MapPlaceAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.itemLayoutRes = R.layout.item_place_result;
    }

    public MapPlaceAdapter(Context context, int itemLayoutRes, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.itemLayoutRes = itemLayoutRes;
    }

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

        if (holder.tvName != null) holder.tvName.setText(place.name);
        if (holder.tvAddress != null) holder.tvAddress.setText(place.address != null ? place.address : "Chưa có địa chỉ");
        if (holder.tvCategory != null) holder.tvCategory.setText(CategoryUtils.getLabel(place.category));

        if (holder.imgPlace != null) {
            Glide.with(context)
                    .load(place.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.imgPlace);
        }

        holder.loadRating(place.id);

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

        boolean isFav = favoriteIds.contains(place.id);
        if (holder.btnFavorite != null) {
            holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
            holder.btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(place);
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(place);
        });

        if (holder.btnDetail != null) {
            holder.btnDetail.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(place);
            });
        }

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
        TextView tvName, tvAddress, tvStatusInfo, tvCategory, tvRatingText;
        View btnDirection;
        Button btnDetail;
        ImageView[] ratingHearts = new ImageView[5];
        private ReviewRepository reviewRepository = new ReviewRepository();

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnDirection = itemView.findViewById(R.id.btnDirections);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            tvRatingText = itemView.findViewById(R.id.tvRatingText);

            ratingHearts[0] = itemView.findViewById(R.id.heart1);
            ratingHearts[1] = itemView.findViewById(R.id.heart2);
            ratingHearts[2] = itemView.findViewById(R.id.heart3);
            ratingHearts[3] = itemView.findViewById(R.id.heart4);
            ratingHearts[4] = itemView.findViewById(R.id.heart5);
        }

        public void loadRating(String placeId) {
            updateHeartUI(0);
            // Mặc định ban đầu hiện đang tải hoặc trống
            if (tvRatingText != null) tvRatingText.setText("");

            reviewRepository.getPlaceRating(placeId, new DataCallback<Review.RatingResponse>() {
                @Override
                public void onSuccess(Review.RatingResponse data) {
                    if (data != null) {
                        if (tvRatingText != null) {
                            // CẬP NHẬT TẠI ĐÂY: Kiểm tra nếu rating là 0.0
                            if (data.average <= 0) {
                                tvRatingText.setText("Chưa có đánh giá");
                            } else {
                                tvRatingText.setText(String.format("(%.1f)", data.average));
                            }
                        }
                        updateHeartUI(data.average);
                    }
                }
                @Override public void onError(String msg) {
                    if (tvRatingText != null) tvRatingText.setText("Chưa có đánh giá");
                    updateHeartUI(0);
                }
            });
        }

        private void updateHeartUI(double rating) {
            for (int i = 0; i < 5; i++) {
                if (ratingHearts[i] != null) {
                    if (rating >= i + 1) {
                        ratingHearts[i].setImageResource(R.drawable.ic_heart_filled);
                        ratingHearts[i].setColorFilter(Color.parseColor("#FFC107"));
                        ratingHearts[i].setAlpha(1.0f);
                    } else if (rating > i && rating < i + 1) {
                        ratingHearts[i].setImageResource(R.drawable.ic_heart_filled);
                        ratingHearts[i].setColorFilter(Color.parseColor("#FFC107"));
                        ratingHearts[i].setAlpha(0.5f);
                    } else {
                        ratingHearts[i].setImageResource(R.drawable.ic_heart);
                        ratingHearts[i].setColorFilter(Color.parseColor("#D3D3D3"));
                        ratingHearts[i].setAlpha(1.0f);
                    }
                }
            }
        }
    }
}