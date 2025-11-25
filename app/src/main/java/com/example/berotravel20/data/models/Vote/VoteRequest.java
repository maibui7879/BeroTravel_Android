package com.example.berotravel20.data.models.Vote;

public class VoteRequest {
    private String target_id;
    private String target_type;
    private String vote_type;

    public VoteRequest(String target_id, String target_type, String vote_type) {
        this.target_id = target_id;
        this.target_type = target_type;
        this.vote_type = vote_type;
    }

    public String getTarget_id() {
        return target_id;
    }

    public String getTarget_type() {
        return target_type;
    }

    public String getVote_type() {
        return vote_type;
    }
}
