package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.common.DataCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseRepository {
    protected <T> void makeCall(Call<T> call, DataCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code() + " " + response.message());
                }
            }
            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }
}
