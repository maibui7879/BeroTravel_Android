package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.VoteApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Vote.Vote;
import com.example.berotravel20.data.remote.RetrofitClient;

import java.util.List;

public class VoteRepository extends BaseRepository {
    private VoteApiService api = RetrofitClient.getInstance().getVoteApi();

    public void upvoteReview(String reviewId, DataCallback<Void> callback) {
        makeCall(api.castVote(new Vote.Request(reviewId, "review", "upvote")), callback);
    }

    public void downvoteReview(String reviewId, DataCallback<Void> callback) {
        makeCall(api.castVote(new Vote.Request(reviewId, "review", "downvote")), callback);
    }

    public void getVotesForReview(String reviewId, DataCallback<List<Vote>> callback) {
        makeCall(api.getVotes(reviewId, "review"), callback);
    }
}
