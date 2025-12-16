package com.example.berotravel20.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Booking.Booking;
import com.example.berotravel20.data.model.Place.Place; // Import Place
import com.example.berotravel20.data.model.User.User;
import com.example.berotravel20.data.repository.BookingRepository;
import com.example.berotravel20.data.repository.UserRepository;
import com.example.berotravel20.data.repository.ReviewRepository; // Import nếu có API lấy review của user

import java.util.List;

public class ProfileViewModel extends ViewModel {

    // 1. Khai báo các Repository
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    // private ReviewRepository reviewRepository;

    // 2. LiveData chứa dữ liệu
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // -- Dữ liệu thống kê & Danh sách --
    private MutableLiveData<List<Place>> favoritePlaces = new MutableLiveData<>();
    private MutableLiveData<Integer> favoriteCount = new MutableLiveData<>();
    private MutableLiveData<Integer> tripCount = new MutableLiveData<>();
    private MutableLiveData<Integer> reviewCount = new MutableLiveData<>();

    public ProfileViewModel() {
        userRepository = new UserRepository();
        bookingRepository = new BookingRepository();
        // reviewRepository = new ReviewRepository();
    }

    // --- GETTERS ---
    public LiveData<User> getUser() { return user; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public LiveData<List<Place>> getFavoritePlaces() { return favoritePlaces; }
    public LiveData<Integer> getFavoriteCount() { return favoriteCount; }
    public LiveData<Integer> getTripCount() { return tripCount; }
    public LiveData<Integer> getReviewCount() { return reviewCount; }


    // --- LOGIC CHÍNH ---

    // 1. Gọi API lấy thông tin Profile
    public void loadUserProfile() {
        isLoading.setValue(true);
        userRepository.getProfile(new DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                isLoading.setValue(false);
                user.setValue(data);

                // Sau khi có User ID, gọi tiếp các API thống kê
                loadUserStats(data.id);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // 2. Hàm tổng hợp gọi các API thống kê (Favorites, Trips, Reviews)
    private void loadUserStats(String userId) {
        // A. Load danh sách yêu thích
        userRepository.getFavorites(new DataCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                if (data != null) {
                    favoritePlaces.setValue(data);      // Lưu danh sách để hiển thị
                    favoriteCount.setValue(data.size()); // Lưu số lượng
                }
            }
            @Override
            public void onError(String message) { /* Log lỗi nhẹ nếu cần */ }
        });

        // B. Load danh sách chuyến đi (Bookings)
        if (userId != null) {
            bookingRepository.getBookings(userId, new DataCallback<List<Booking>>() {
                @Override
                public void onSuccess(List<Booking> data) {
                    if (data != null) {
                        tripCount.setValue(data.size());
                    } else {
                        tripCount.setValue(0);
                    }
                }
                @Override
                public void onError(String message) { /* Log lỗi nhẹ */ }
            });
        }

        // C. Load số lượng Review
        // (Hiện tại ReviewRepository chưa có hàm lấy review theo UserID,
        // nên ta tạm set là 0 hoặc Mock data. Nếu sau này Backend bổ sung API, bạn gọi ở đây)
        reviewCount.setValue(0);
    }

    // 3. Cập nhật Profile (Logic cũ của bạn, giữ nguyên)
    public void updateProfile(String name, String bio, String dob, String avatarUrlOrBase64, String coverUrlOrBase64) {
        isLoading.setValue(true);

        User updatedUser = new User();
        updatedUser.name = name;
        updatedUser.bio = bio;
        updatedUser.dob = dob;
        updatedUser.avatarUrl = avatarUrlOrBase64;
        updatedUser.coverUrl = coverUrlOrBase64;

        userRepository.updateProfile(updatedUser, new DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                isLoading.setValue(false);
                user.setValue(data);
                errorMessage.setValue("Cập nhật thành công!");
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}