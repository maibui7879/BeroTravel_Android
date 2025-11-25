package com.example.berotravel20.data.api;

import com.example.berotravel20.data.models.Vote.Vote;
import com.example.berotravel20.data.models.Vote.VoteDeleteResponse;
import com.example.berotravel20.data.models.Vote.VoteRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VoteService {

    @POST("votes")
    Call<Vote> createOrUpdateVote(@Body VoteRequest request);

    @GET("votes")
    Call<List<Vote>> getVotes(
            @Query("target_id") String targetId,
            @Query("target_type") String targetType
    );

    @DELETE("votes/{id}")
    Call<VoteDeleteResponse> deleteVote(@Path("id") String id);
}
