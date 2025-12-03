package com.example.berotravel20.data.model.User;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id") public String id;
    public String name;
    public String email;
    @SerializedName("avatar_url") public String avatarUrl;
    @SerializedName("cover_url") public String coverUrl;
    public String dob;
    public String bio;

    // Class rút gọn dùng trong Review/Vote
    public static class Brief {
        @SerializedName("_id") public String id;
        public String name;
        @SerializedName("avatar_url") public String avatarUrl;
    }
}