package com.example.berotravel20.ui.main.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.ReviewAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Review.Review;
import com.example.berotravel20.data.repository.ReviewRepository;
import com.example.berotravel20.data.repository.VoteRepository;
import com.example.berotravel20.ui.common.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class ReviewTabFragment extends BaseFragment {
    private String placeId;
    private ReviewAdapter reviewAdapter;
    private ReviewRepository reviewRepository;
    private VoteRepository voteRepository;

    private TextView tvAvgRating, tvTotalReviews;
    private RatingBar ratingBarAvg, ratingInput;
    private EditText etComment;
    private Button btnSubmit;

    public static ReviewTabFragment newInstance(String id) {
        ReviewTabFragment f = new ReviewTabFragment();
        Bundle b = new Bundle();
        b.putString("id", id);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reviewRepository = new ReviewRepository();
        voteRepository = new VoteRepository();
        if (getArguments() != null) placeId = getArguments().getString("id");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadReviewData();
    }

    private void initViews(View view) {
        tvAvgRating = view.findViewById(R.id.tv_avg_rating);
        tvTotalReviews = view.findViewById(R.id.tv_total_reviews);
        ratingBarAvg = view.findViewById(R.id.rating_bar_avg);
        ratingInput = view.findViewById(R.id.rating_input);
        etComment = view.findViewById(R.id.et_comment);
        btnSubmit = view.findViewById(R.id.btn_submit_review);

        // Khởi tạo RecyclerView
        RecyclerView rv = view.findViewById(R.id.rv_reviews_booking);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy UserID từ TokenManager (Giả định TokenManager có hàm getUserId)
        String currentUserId = (tokenManager != null) ? tokenManager.getUserId() : null;

        reviewAdapter = new ReviewAdapter(new ArrayList<>(), currentUserId, (review, voteType) -> {
            // Kiểm tra đăng nhập trước khi Like/Dislike
            if (!isUserLoggedIn()) {
                requireLogin();
                return;
            }
            handleVote(review.id, voteType);
        });
        rv.setAdapter(reviewAdapter);

        btnSubmit.setOnClickListener(v -> postReview());
    }

    private void handleVote(String reviewId, String voteType) {
        DataCallback<Void> callback = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Tải lại để cập nhật số lượng Vote và màu sắc Icon
                loadReviewData();
            }
            @Override
            public void onError(String msg) {
                // LOG LỖI THẬT SỰ TẠI ĐÂY
                android.util.Log.e("VOTE_ERROR", "Server trả về lỗi: " + msg);
                showError("Vote thất bại!");
            }
        };

        if ("upvote".equals(voteType)) {
            voteRepository.upvoteReview(reviewId, callback);
        } else {
            voteRepository.downvoteReview(reviewId, callback);
        }
    }

    private void loadReviewData() {
        // 1. Lấy danh sách review
        reviewRepository.getReviewsByPlace(placeId, new DataCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> data) {
                if (!isAdded()) return;
                reviewAdapter.setReviews(data);
                tvTotalReviews.setText(data.size() + " đánh giá");
            }
            @Override public void onError(String msg) { showError(msg); }
        });

        // 2. Lấy điểm đánh giá trung bình
        reviewRepository.getPlaceRating(placeId, new DataCallback<Review.RatingResponse>() {
            @Override
            public void onSuccess(Review.RatingResponse data) {
                if (!isAdded()) return;
                tvAvgRating.setText(String.format("%.1f", data.average));
                ratingBarAvg.setRating((float) data.average);
            }
            @Override public void onError(String msg) {}
        });
    }

    private void postReview() {
        // Kiểm tra đăng nhập trước khi cho phép post review
        if (!isUserLoggedIn()) {
            requireLogin();
            return;
        }

        int rating = (int) ratingInput.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) { showError("Vui lòng chọn số sao!"); return; }
        if (comment.isEmpty()) { showError("Vui lòng nhập nội dung đánh giá!"); return; }

        showLoading(); // Dùng phương thức từ BaseFragment
        reviewRepository.createReview(placeId, rating, comment, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                showSuccess("Đăng đánh giá thành công!");
                etComment.setText("");
                ratingInput.setRating(0);
                loadReviewData(); // Tải lại danh sách
            }
            @Override public void onError(String msg) {
                hideLoading();
                showError(msg);
            }
        });
    }
}