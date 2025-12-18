package com.example.berotravel20.ui.main.journey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.adapters.JourneyAdapter;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.data.repository.JourneyRepository;
import com.example.berotravel20.ui.common.BaseFragment;
import com.example.berotravel20.ui.common.SearchPlaceBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class JourneyFragment extends BaseFragment {

    private JourneyRepository repository;
    private RecyclerView rvJourneys;
    private FloatingActionButton fabCreate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journey, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new JourneyRepository(requireContext());
        rvJourneys = view.findViewById(R.id.rv_journey_list);
        fabCreate = view.findViewById(R.id.fab_create_journey);

        rvJourneys.setLayoutManager(new LinearLayoutManager(getContext()));

        if (fabCreate != null) {
            fabCreate.setOnClickListener(v -> showSearchToCreate());
        }

        loadData();
    }

    private void loadData() {
        showLoading();
        repository.getJourneys(new DataCallback<List<Journey>>() {
            @Override
            public void onSuccess(List<Journey> data) {
                hideLoading();

                JourneyAdapter adapter = new JourneyAdapter(requireContext(), data, new JourneyAdapter.OnJourneyClickListener() {
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

            @Override
            public void onError(String msg) {
                hideLoading();
                showError(msg);
            }
        });
    }

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
                hideLoading();
                showSuccess("Tạo hành trình thành công!");
                loadData();
            }

            @Override
            public void onError(String m) {
                hideLoading();
                showError(m);
            }
        });
    }

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
                hideLoading();
                showSuccess("Đã xóa hành trình!");
                loadData();
            }

            @Override
            public void onError(String m) {
                hideLoading();
                showError(m);
            }
        });
    }

    private void openJourneyDetail(String journeyId) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.base_container, JourneyDetailFragment.newInstance(journeyId))
                .addToBackStack(null)
                .commit();
    }
}