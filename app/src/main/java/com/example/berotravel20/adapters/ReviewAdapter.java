package com.example.berotravel20.adapters;

import android.graphics.Color;
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
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Review.Review;
import com.example.berotravel20.data.model.Vote.Vote;
import com.example.berotravel20.data.model.Vote.VoteResponse;
import com.example.berotravel20.data.repository.VoteRepository;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private OnVoteClickListener voteListener;
    private String currentUserId; // ID của user đang đăng nhập

    public interface OnVoteClickListener {
        void onVote(Review review, String voteType);
    }

    public ReviewAdapter(List<Review> reviews, String currentUserId, OnVoteClickListener voteListener) {
        this.reviews = reviews;
        this.currentUserId = currentUserId;
        this.voteListener = voteListener;
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
        holder.bind(review, currentUserId, voteListener);
    }

    @Override
    public int getItemCount() {
        return reviews == null ? 0 : reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnLike, btnDislike;
        TextView tvName, tvComment, tvVoteCount;
        RatingBar ratingBar;
        VoteRepository voteRepository; // Dùng để fetch data tại chỗ cho từng item

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_username);
            tvComment = itemView.findViewById(R.id.tv_comment);
            ratingBar = itemView.findViewById(R.id.item_rating_bar);
            voteRepository = new VoteRepository();
        }

        public void bind(Review review, String currentUserId, OnVoteClickListener listener) {
            // 1. Đổ dữ liệu tĩnh từ model Review
            tvComment.setText(review.comment);
            ratingBar.setRating(review.rating);
            tvVoteCount.setText(String.valueOf(review.voteScore));

            if (review.user != null) {
                tvName.setText(review.user.name);
                Glide.with(itemView.getContext())
                        .load(review.user.avatarUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .circleCrop()
                        .into(imgAvatar);
            }

            // 2. Fetch dữ liệu Vote chi tiết cho item này
            resetVoteUI();
            fetchVoteDetails(review.id, currentUserId);

            // 3. Sự kiện Click (Giao diện Like/Dislike nhưng gửi Upvote/Downvote cho API)
            btnLike.setOnClickListener(v -> {
                if (listener != null) listener.onVote(review, "upvote");
            });

            btnDislike.setOnClickListener(v -> {
                if (listener != null) listener.onVote(review, "downvote");
            });
        }

        private void fetchVoteDetails(String reviewId, String currentUserId) {
            voteRepository.getVotesForReview(reviewId, new DataCallback<VoteResponse>() {
                @Override
                public void onSuccess(VoteResponse data) {
                    // Tính toán tổng điểm: Up - Down
                    int totalScore = data.summary.up - data.summary.down;
                    tvVoteCount.setText(String.valueOf(totalScore));

                    // Kiểm tra xem User hiện tại đã vote chưa để highlight icon
                    if (currentUserId != null && data.votes != null) {
                        for (Vote v : data.votes) {
                            if (currentUserId.equals(v.userId)) {
                                highlightIcon(v.voteType);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onError(String msg) {
                    // Nếu lỗi thì giữ nguyên voteScore mặc định từ model Review
                }
            });
        }

        private void resetVoteUI() {
            int gray = Color.parseColor("#888888");
            btnLike.setColorFilter(gray);
            btnDislike.setColorFilter(gray);
        }

        private void highlightIcon(String type) {
            if ("upvote".equals(type)) {
                btnLike.setColorFilter(Color.parseColor("#1E88E5")); // Màu xanh cho Like
            } else if ("downvote".equals(type)) {
                btnDislike.setColorFilter(Color.parseColor("#F44336")); // Màu đỏ cho Dislike
            }
        }
    }
}