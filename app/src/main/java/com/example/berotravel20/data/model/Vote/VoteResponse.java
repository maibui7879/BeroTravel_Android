package com.example.berotravel20.data.model.Vote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VoteResponse {
    @SerializedName("votes") public List<Vote> votes;
    @SerializedName("summary") public Summary summary;

    public static class Summary {
        @SerializedName("up") public int up;
        @SerializedName("down") public int down;
    }
}