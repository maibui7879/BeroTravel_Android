package com.example.berotravel20.data.models.Vote;

public class Vote {
    private String _id;
    private String user_id;
    private String target_id;
    private String target_type;
    private String vote_type;

    public String get_id() {
        return _id;
    }

    public String getUser_id() {
        return user_id;
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
