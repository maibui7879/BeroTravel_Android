package com.example.berotravel20.ui.main.journey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.RoadmapAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.data.repository.JourneyRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.common.SearchPlaceBottomSheet;

import java.util.ArrayList;
import java.util.List;

public class JourneyDetailFragment extends BaseFragment {

    // --- KHAI BÁO BIẾN ---
    private JourneyRepository repository;
    private RecyclerView rvRoadmap;
    private TextView tvStatus;
    private Button btnStartNavigation;
    private Toolbar toolbar;
    private View btnAddPlace; // Nút thêm địa điểm

    private Journey currentJourney; // Dữ liệu hành trình hiện tại

    public static JourneyDetailFragment newInstance(String id) {
        JourneyDetailFragment f = new JourneyDetailFragment();
        Bundle b = new Bundle();
        b.putString("ID", id);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journey_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        rvRoadmap = view.findViewById(R.id.rv_roadmap);
        tvStatus = view.findViewById(R.id.tv_journey_status_detail);
        btnStartNavigation = view.findViewById(R.id.btn_start_navigation);
        toolbar = view.findViewById(R.id.toolbar_journey_detail);
        btnAddPlace = view.findViewById(R.id.btn_add_place_detail); // Nút (+)

        // 2. Khởi tạo Repository & RecyclerView
        repository = new JourneyRepository(requireContext());
        rvRoadmap.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Xử lý nút Back
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // 4. Sự kiện nút Bắt đầu (Logic thông minh: Check status -> Navigate)
        btnStartNavigation.setOnClickListener(v -> handleStartNavigation());

        // 5. Sự kiện nút Thêm địa điểm (Logic: BottomSheet -> API Update)
        if (btnAddPlace != null) {
            btnAddPlace.setOnClickListener(v -> showSearchToAdd());
        }

        // 6. Tải dữ liệu
        loadJourneyData();
    }

    private void showSearchToAdd() {
        SearchPlaceBottomSheet sheet = new SearchPlaceBottomSheet();
        sheet.setListener(place -> {
            // Khi chọn địa điểm mới -> Thêm vào list ID -> Gọi API Update
            if (currentJourney != null) {
                List<String> newIds = getListIdsFromJourney(currentJourney);
                newIds.add(place.id); // Thêm ID mới
                updateJourneyList(newIds);
            }
        });
        sheet.show(getParentFragmentManager(), "AddPlace");
    }

    private void confirmDeletePlace(String realPlaceId) {
        // Khi xóa địa điểm -> Xóa khỏi list ID -> Gọi API Update
        if (currentJourney != null) {
            List<String> newIds = getListIdsFromJourney(currentJourney);
            newIds.remove(realPlaceId);
            updateJourneyList(newIds);
        }
    }

    // Hàm chung để cập nhật danh sách địa điểm (PUT)
    private void updateJourneyList(List<String> newIds) {
        showLoading();
        repository.updateJourneyPlaces(currentJourney.id, newIds, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void d) {
                hideLoading();
                showSuccess("Cập nhật lộ trình thành công!");
                loadJourneyData(); // Reload lại dữ liệu mới nhất
            }

            @Override
            public void onError(String m) {
                hideLoading();
                showError(m);
            }
        });
    }

    // Helper: Lấy danh sách ID từ object Journey
    private List<String> getListIdsFromJourney(Journey j) {
        List<String> ids = new ArrayList<>();
        if (j.places != null) {
            for (Journey.JourneyPlace jp : j.places) {
                if (jp.place != null) {
                    ids.add(jp.place.id);
                }
            }
        }
        return ids;
    }

    private void handleStartNavigation() {
        if (currentJourney == null) {
            showError("Dữ liệu đang tải...");
            return;
        }

        if (currentJourney.places == null || currentJourney.places.isEmpty()) {
            showError("Hành trình chưa có địa điểm nào!");
            return;
        }

        // Kiểm tra trạng thái
        String status = currentJourney.status != null ? currentJourney.status.toLowerCase() : "";

        if (status.equals("suspended")) {
            // Case 1: Đang tạm dừng -> Kích hoạt lại -> Đi
            activateJourneyAndNavigate();
        } else {
            // Case 2: Đang chạy hoặc chưa bắt đầu -> Đi luôn
            navigateToMap();
        }
    }

    private void activateJourneyAndNavigate() {
        showLoading();
        repository.updateStatus(currentJourney.id, "ongoing", new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                Toast.makeText(getContext(), "Hành trình đã được tiếp tục!", Toast.LENGTH_SHORT).show();

                // Cập nhật local status để truyền sang màn hình sau cho đúng
                currentJourney.status = "ongoing";
                tvStatus.setText("Trạng thái: ONGOING");

                navigateToMap();
            }

            @Override
            public void onError(String msg) {
                hideLoading();
                showError("Không thể kích hoạt hành trình: " + msg);
            }
        });
    }

    private void navigateToMap() {
        // Chuyển sang NavigationFragment
        NavigationFragment navFrag = NavigationFragment.newInstance(currentJourney);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.base_container, navFrag)
                .addToBackStack(null)
                .commit();
    }

    private void loadJourneyData() {
        if (getArguments() == null) return;
        String id = getArguments().getString("ID");
        if (id == null) return;

        showLoading();
        repository.getJourneyDetail(id, new DataCallback<Journey>() {
            @Override
            public void onSuccess(Journey data) {
                hideLoading();
                if (data != null) {
                    currentJourney = data;

                    // Cập nhật UI

                    tvStatus.setText("Trạng thái: " + (data.status != null ? data.status.toUpperCase() : ""));

                    // Setup Adapter với sự kiện Xóa
                    if (data.places != null) {
                        RoadmapAdapter adapter = new RoadmapAdapter(data.places);
                        adapter.setOnDeleteListener(item -> {
                            if (item.place != null) {
                                confirmDeletePlace(item.place.id);
                            }
                        });
                        rvRoadmap.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                hideLoading();
                showError("Lỗi tải dữ liệu: " + msg);
            }
        });
    }
}