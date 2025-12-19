package com.example.berotravel20.ui.main.journey;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.JourneyAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.data.repository.JourneyRepository;
import com.example.berotravel20.ui.auth.AuthActivity;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.common.RequestLoginDialog;
import com.example.berotravel20.ui.common.SearchPlaceBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class JourneyFragment extends BaseFragment implements RequestLoginDialog.RequestLoginListener {

    private JourneyRepository repository;
    private RecyclerView rvJourneys;
    private FloatingActionButton fabCreate;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout llEmptyJourney;

    private JourneyAdapter adapter;
    private List<Journey> journeyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journey, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo View
        initViews(view);
        setupRecyclerView();

        // 2. Kiểm tra đăng nhập
        if (!isUserLoggedIn()) {
            showLoginRequestDialog();
            return;
        }

        // 3. Khởi tạo Repository
        repository = new JourneyRepository(requireContext());

        // 4. Setup Events & Load Data
        setupEvents();
        loadData(true); // Hiển thị loading lần đầu
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUserLoggedIn() && repository != null) {
            loadData(false); // Reload ngầm khi quay lại
        }
    }

    private void initViews(View view) {
        rvJourneys = view.findViewById(R.id.rv_journey_list);
        fabCreate = view.findViewById(R.id.fab_create_journey);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_journey);
        llEmptyJourney = view.findViewById(R.id.ll_empty_journey);
    }

    private void setupRecyclerView() {
        rvJourneys.setLayoutManager(new LinearLayoutManager(getContext()));
        // Khởi tạo adapter rỗng trước để tránh lỗi null
        adapter = new JourneyAdapter(requireContext(), journeyList, new JourneyAdapter.OnJourneyClickListener() {
            @Override
            public void onClick(Journey journey) {
                openJourneyDetail(journey.id);
            }

            @Override
            public void onDelete(Journey journey) {
                confirmDelete(journey);
            }
        });
        rvJourneys.setAdapter(adapter);
    }

    private void setupEvents() {
        fabCreate.setOnClickListener(v -> showSearchToCreate());

        // Xử lý kéo để làm mới
        swipeRefreshLayout.setOnRefreshListener(() -> loadData(false));
        swipeRefreshLayout.setColorSchemeResources(R.color.teal_700);
    }

    private void loadData(boolean showBaseLoading) {
        if (showBaseLoading) showLoading();

        repository.getJourneys(new DataCallback<List<Journey>>() {
            @Override
            public void onSuccess(List<Journey> data) {
                // KIỂM TRA: Nếu Fragment đã bị hủy thì không làm gì tiếp
                if (!isAdded() || getContext() == null) return;

                hideLoading();
                swipeRefreshLayout.setRefreshing(false);

                journeyList.clear();
                if (data != null && !data.isEmpty()) {
                    journeyList.addAll(data);
                    llEmptyJourney.setVisibility(View.GONE);
                    rvJourneys.setVisibility(View.VISIBLE);
                } else {
                    llEmptyJourney.setVisibility(View.VISIBLE);
                    rvJourneys.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String msg) {
                if (!isAdded()) return;
                hideLoading();
                swipeRefreshLayout.setRefreshing(false);
                showError(msg);
            }
        });
    }

    // --- LOGIC TẠO MỚI ---
    private void showSearchToCreate() {
        SearchPlaceBottomSheet sheet = new SearchPlaceBottomSheet();
        sheet.setListener(place -> {
            List<String> places = new ArrayList<>();
            places.add(place.id);
            createJourney(places);
        });
        sheet.show(getParentFragmentManager(), "CreateJourney");
    }

    private void createJourney(List<String> places) {
        showLoading();
        repository.createJourney(places, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void d) {
                if (!isAdded()) return;
                hideLoading();
                showSuccess("Tạo hành trình thành công!");
                loadData(false);
            }
            @Override
            public void onError(String m) {
                if (!isAdded()) return;
                hideLoading();
                showError(m);
            }
        });
    }

    // --- LOGIC XÓA ---
    private void confirmDelete(Journey j) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa hành trình?")
                .setMessage("Bạn có chắc muốn xóa hành trình này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteJourney(j.id))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteJourney(String id) {
        showLoading();
        repository.deleteJourney(id, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void d) {
                if (!isAdded()) return;
                hideLoading();
                showSuccess("Đã xóa hành trình!");
                loadData(false);
            }
            @Override public void onError(String m) {
                if (!isAdded()) return;
                hideLoading();
                showError(m);
            }
        });
    }

    private void openJourneyDetail(String journeyId) {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.base_container, JourneyDetailFragment.newInstance(journeyId))
                .addToBackStack(null)
                .commit();
    }

    private void showLoginRequestDialog() {
        RequestLoginDialog dialog = RequestLoginDialog.newInstance();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "RequestLoginDialog");
    }

    @Override public void onLoginClick() {
        startActivity(new Intent(requireContext(), AuthActivity.class));
    }

    @Override public void onCancelClick() { }
}