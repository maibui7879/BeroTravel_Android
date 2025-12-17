package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Vote.Vote;
import com.example.berotravel20.data.model.Vote.VoteResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface VoteApiService {
    @POST("/api/votes")
    Call<Void> castVote(@Body Vote.Request request);

    @GET("/api/votes")
    Call<VoteResponse> getVotes( // Đổi từ List<Vote> sang VoteResponse
                                 @Query("target_id") String targetId,
                                 @Query("target_type") String targetType
    );

    @DELETE("/api/votes/{id}")
    Call<Void> deleteVote(@Path("id") String id);
}
