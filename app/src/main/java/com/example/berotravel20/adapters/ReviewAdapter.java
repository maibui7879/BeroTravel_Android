package com.example.berotravel20.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Review.Review;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews == null ? 0 : reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvComment;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            // ÁNH XẠ CÁC VIEW TỪ FILE item_review.xml
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_username);
            tvComment = itemView.findViewById(R.id.tv_comment);
            ratingBar = itemView.findViewById(R.id.item_rating_bar);
        }

        public void bind(Review review) {
            // Đổ dữ liệu bình luận
            if (review.comment != null) {
                tvComment.setText(review.comment);
            } else {
                tvComment.setText("");
            }

            // Đổ dữ liệu số sao
            ratingBar.setRating(review.rating);

            // Đổ dữ liệu người dùng (Tên và Ảnh đại diện)
            if (review.user != null) {
                tvName.setText(review.user.name);
                Glide.with(itemView.getContext())
                        .load(review.user.avatarUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                tvName.setText("Người dùng ẩn danh");
                imgAvatar.setImageResource(R.drawable.account_icon);
            }
        }
    }
}