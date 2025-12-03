package com.example.berotravel20.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.User.AuthPayload;
import com.example.berotravel20.data.repository.UserRepository;

public class AuthViewModel extends ViewModel {
    private UserRepository repository;

    // LiveData
    private MutableLiveData<AuthPayload.AuthResponse> loginResponse = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel() {
        repository = new UserRepository();
    }

    public LiveData<AuthPayload.AuthResponse> getLoginResponse() { return loginResponse; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void login(String email, String password) {
        isLoading.setValue(true);
        repository.login(email, password, new DataCallback<AuthPayload.AuthResponse>() {
            @Override
            public void onSuccess(AuthPayload.AuthResponse data) {
                isLoading.setValue(false);
                loginResponse.setValue(data);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void register(String name, String email, String password) {
        isLoading.setValue(true);
        repository.register(name, email, password, new DataCallback<AuthPayload.AuthResponse>() {
            @Override
            public void onSuccess(AuthPayload.AuthResponse data) {
                isLoading.setValue(false);
                loginResponse.setValue(data); // Đăng ký xong coi như login thành công
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}
