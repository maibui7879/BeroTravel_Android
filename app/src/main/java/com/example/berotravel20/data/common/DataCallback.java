package com.example.berotravel20.data.common;

public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(String message);


}
