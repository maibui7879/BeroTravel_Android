package com.example.berotravel20.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.repository.UserRepository;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private UserRepository repository;

    // LiveData để Fragment lắng nghe
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<Integer> favoriteCount = new MutableLiveData<>(); // Để hiện Stats
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public ProfileViewModel() {
        repository = new UserRepository();
    }

    // Getters
    public LiveData<User> getUser() { return user; }
    public LiveData<Integer> getFavoriteCount() { return favoriteCount; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // 1. Gọi API lấy thông tin Profile
    public void loadUserProfile() {
        isLoading.setValue(true);
        repository.getProfile(new DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                isLoading.setValue(false);
                user.setValue(data);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // 2. Gọi API lấy danh sách yêu thích -> Để đếm số lượng (Làm Stats)
    public void loadUserStats() {
        repository.getFavorites(new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                // API trả về List ID, mình lấy size() để biết có bao nhiêu cái yêu thích
                if (data != null) {
                    favoriteCount.setValue(data.size());
                } else {
                    favoriteCount.setValue(0);
                }
            }

            @Override
            public void onError(String message) {
                // Kệ lỗi này, không ảnh hưởng flow chính
            }
        });
    }

    // 3. Cập nhật Profile
    public void updateProfile(String name, String bio, String dob) {
        isLoading.setValue(true);
        User updatedUser = new User();
        updatedUser.name = name;
        updatedUser.bio = bio;
        updatedUser.dob = dob;

        repository.updateProfile(updatedUser, new DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                isLoading.setValue(false);
                user.setValue(data); // Cập nhật lại UI với data mới
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}