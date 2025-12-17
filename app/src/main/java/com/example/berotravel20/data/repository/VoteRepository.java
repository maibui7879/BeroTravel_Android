package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.VoteApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Vote.Vote;
import com.example.berotravel20.data.model.Vote.VoteResponse; // Import mới
import com.example.berotravel20.data.remote.RetrofitClient;

public class VoteRepository extends BaseRepository {
    private VoteApiService api = RetrofitClient.getInstance().getVoteApi();

    public void upvoteReview(String reviewId, DataCallback<Void> callback) {
        android.util.Log.d("VOTE_DEBUG", "Gửi Upvote - ID: " + reviewId + " | Type: review");
        makeCall(api.castVote(new Vote.Request(reviewId, "Review", "upvote")), callback);
    }

    public void downvoteReview(String reviewId, DataCallback<Void> callback) {
        android.util.Log.d("VOTE_DEBUG", "Gửi Downvote - ID: " + reviewId + " | Type: review");
        makeCall(api.castVote(new Vote.Request(reviewId, "Review", "downvote")), callback);
    }

    // Sửa DataCallback từ List<Vote> thành VoteResponse
    public void getVotesForReview(String reviewId, DataCallback<VoteResponse> callback) {
        makeCall(api.getVotes(reviewId, "review"), callback);
    }

    // Thêm hàm xóa vote (Nếu bạn muốn làm tính năng bỏ like/dislike)
    public void deleteVote(String voteId, DataCallback<Void> callback) {
        makeCall(api.deleteVote(voteId), callback);
    }
}