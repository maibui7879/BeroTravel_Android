package com.example.berotravel20.ui.main.place;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import com.example.berotravel20.models.ReviewResponse;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private OnVoteClickListener voteListener;
    private List<ReviewResponse> reviews;

    public interface OnVoteClickListener {
        void onVote(ReviewResponse review, String voteType);
    }

    public ReviewAdapter(List<ReviewResponse> reviews, OnVoteClickListener voteListener) {
        this.reviews = reviews;
        this.voteListener = voteListener;
    }

    public void setReviews(List<ReviewResponse> reviews) {
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
        ReviewResponse review = reviews.get(position);
        holder.bind(review, voteListener);
    }

    @Override
    public int getItemCount() {
        return reviews == null ? 0 : reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvDate, tvRating, tvComment;
        ImageView btnUpvote, btnDownvote;
        TextView tvVoteCount;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_username);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvComment = itemView.findViewById(R.id.tv_comment);
            btnUpvote = itemView.findViewById(R.id.btn_upvote);
            btnDownvote = itemView.findViewById(R.id.btn_downvote);
            tvVoteCount = itemView.findViewById(R.id.tv_vote_count);
        }

        public void bind(ReviewResponse review, OnVoteClickListener listener) {
            tvComment.setText(review.comment);
            tvRating.setText(String.valueOf(review.rating));

            // Vote UI
            tvVoteCount.setText(String.valueOf(review.vote_score));

            // Reset tints
            btnUpvote.setColorFilter(android.graphics.Color.parseColor("#888888"));
            btnDownvote.setColorFilter(android.graphics.Color.parseColor("#888888"));

            if ("upvote".equals(review.user_vote)) {
                btnUpvote.setColorFilter(android.graphics.Color.parseColor("#2E8B57")); // Green
            } else if ("downvote".equals(review.user_vote)) {
                btnDownvote.setColorFilter(android.graphics.Color.parseColor("#FF6347")); // Red
            }

            btnUpvote.setOnClickListener(v -> {
                if (listener != null)
                    listener.onVote(review, "upvote");
            });

            btnDownvote.setOnClickListener(v -> {
                if (listener != null)
                    listener.onVote(review, "downvote");
            });

            if (review.user_id != null) {
                tvName.setText(review.user_id.name);
                if (review.user_id.avatar_url != null) {
                    Glide.with(itemView.getContext())
                            .load(review.user_id.avatar_url)
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .into(imgAvatar);
                }
            } else {
                tvName.setText("Unknown User");
            }

            if (review.createdAt != null && review.createdAt.length() >= 10) {
                tvDate.setText(review.createdAt.substring(0, 10));
            }
        }
    }
}
