package com.example.berotravel20.ui.common;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.berotravel20.data.local.TokenManager;
import com.example.berotravel20.network.ApiClient;
import com.example.berotravel20.network.ApiService;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.utils.ToastUtils;

public abstract class BaseFragment extends Fragment {

    protected ApiService apiService;
    protected TokenManager tokenManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            // Khởi tạo các dịch vụ dùng chung
            apiService = ApiClient.getClient(getContext()).create(ApiService.class);
            tokenManager = TokenManager.getInstance(getContext());
        }
    }

    // --- 1. QUẢN LÝ LOADING (GỌI TỪ BASE ACTIVITY) ---

    protected void showLoading() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showLoading();
        }
    }

    protected void hideLoading() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).hideLoading();
        }
    }

    // --- 2. ĐIỀU HƯỚNG (GỌI TỪ BASE ACTIVITY) ---

    protected void replaceFragment(Fragment fragment) {
        if (getActivity() instanceof BaseActivity) {
            // Gọi hàm navigateToDetail để ĐƯỢC LƯU VÀO BACKSTACK
            ((BaseActivity) getActivity()).navigateToDetail(fragment);
        }
    }

    protected void onBack() {
        if (getActivity() != null) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                getActivity().onBackPressed();
            }
        }
    }

    // --- 3. HIỂN THỊ THÔNG BÁO (TOAST) ---

    protected void showSuccess(String message) {
        if (getContext() != null) ToastUtils.showSuccess(getContext(), message);
    }

    protected void showError(String message) {
        if (getContext() != null) ToastUtils.showError(getContext(), message);
    }

    protected void showWarning(String message) {
        if (getContext() != null) ToastUtils.showWarning(getContext(), message);
    }

    // --- 4. KIỂM TRA ĐĂNG NHẬP & DIALOG YÊU CẦU ---

    protected boolean isUserLoggedIn() {
        return tokenManager != null && tokenManager.getToken() != null && !tokenManager.getToken().isEmpty();
    }

    /**
     * Hiển thị Dialog yêu cầu đăng nhập thay vì chuyển trang trực tiếp.
     */
    protected void requireLogin() {
        if (getActivity() != null) {
            RequestLoginDialog dialog = RequestLoginDialog.newInstance();

            // Thiết lập Listener để xử lý nút bấm trong Dialog
            dialog.setListener(new RequestLoginDialog.RequestLoginListener() {
                @Override
                public void onLoginClick() {
                    // Khi user bấm "Đăng nhập" trên Dialog
                    Intent intent = new Intent(getContext(), AuthActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCancelClick() {
                    // User đóng dialog, không làm gì thêm
                }
            });

            dialog.show(getChildFragmentManager(), "RequestLoginDialog");
        }
    }
}