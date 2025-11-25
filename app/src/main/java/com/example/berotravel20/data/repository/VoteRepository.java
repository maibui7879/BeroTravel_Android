package com.example.berotravel20.data.repository;

import android.content.Context;

import com.example.berotravel20.data.api.VoteService;
import com.example.berotravel20.data.api.RetrofitClient;
import com.example.berotravel20.data.local.SessionManager;
import com.example.berotravel20.data.models.Vote.Vote;
import com.example.berotravel20.data.models.Vote.VoteDeleteResponse;
import com.example.berotravel20.data.models.Vote.VoteRequest;

import java.util.List;

import retrofit2.Call;

public class VoteRepository {

    private final VoteService service;

    public VoteRepository(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        service = RetrofitClient.getClient(sessionManager).create(VoteService.class);
    }

    public Call<Vote> createOrUpdateVote(VoteRequest request) {
        return service.createOrUpdateVote(request);
    }

    public Call<List<Vote>> getVotes(String targetId, String targetType) {
        return service.getVotes(targetId, targetType);
    }

    public Call<VoteDeleteResponse> deleteVote(String id) {
        return service.deleteVote(id);
    }
}
