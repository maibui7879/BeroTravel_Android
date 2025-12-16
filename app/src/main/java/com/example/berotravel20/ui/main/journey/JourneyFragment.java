package com.example.berotravel20.ui.main.journey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.network.TokenManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import android.widget.Spinner;
import android.widget.TextView;

public class JourneyFragment extends Fragment implements JourneyAdapter.OnDeleteClickListener {

    private RecyclerView rvJourneys;
    private FloatingActionButton fabCreateTrip;
    private JourneyAdapter adapter;
    private List<Journey> journeyList;

    public JourneyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journey, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvJourneys = view.findViewById(R.id.rv_journeys);
        fabCreateTrip = view.findViewById(R.id.fab_create_trip);

        rvJourneys.setLayoutManager(new LinearLayoutManager(getContext()));
        journeyList = new ArrayList<>();
        adapter = new JourneyAdapter(getContext(), journeyList, this);
        adapter.setOnItemClickListener(journeyId -> {
            android.content.Intent intent = new android.content.Intent(getContext(), JourneyDetailActivity.class);
            intent.putExtra("JOURNEY_ID", journeyId);
            startActivity(intent);
        });
        rvJourneys.setAdapter(adapter);

        fabCreateTrip.setOnClickListener(v -> showCreateTripDialog());

        fetchJourneys();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchJourneys();
    }

    private void fetchJourneys() {
        TokenManager tokenManager = new TokenManager(getContext());
        if (tokenManager.getToken() == null) {
            startActivity(
                    new android.content.Intent(getContext(), com.example.berotravel20.ui.auth.AuthActivity.class));
            return;
        }

        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getJourneyApi()
                .getJourneys()
                .enqueue(new retrofit2.Callback<com.example.berotravel20.models.JourneyResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.berotravel20.models.JourneyResponse> call,
                            retrofit2.Response<com.example.berotravel20.models.JourneyResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            journeyList.clear();
                            if (response.body().data != null) {
                                journeyList.addAll(response.body().data);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải lịch trình", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.berotravel20.models.JourneyResponse> call,
                            Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        android.util.Log.e("JourneyFragment", "Fetch error", t);
                    }
                });
    }

    @Override
    public void onDeleteClick(String journeyId) {
        if (journeyId == null)
            return;

        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getJourneyApi()
                .deleteJourney(journeyId)
                .enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call,
                            retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã xóa chuyến đi", Toast.LENGTH_SHORT).show();
                            fetchJourneys(); // Refresh list
                        } else {
                            try {
                                String error = response.errorBody().string();
                                org.json.JSONObject json = new org.json.JSONObject(error);
                                Toast.makeText(getContext(), json.optString("message", "Lỗi xóa"), Toast.LENGTH_SHORT)
                                        .show();
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Không thể xóa", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCreateTripDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_trip, null);
        builder.setView(view);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        Spinner spinner = view.findViewById(R.id.spinner_location);
        TextView tvStart = view.findViewById(R.id.tv_start_date);
        TextView tvEnd = view.findViewById(R.id.tv_end_date);
        View btnCancel = view.findViewById(R.id.btn_cancel);
        View btnCreate = view.findViewById(R.id.btn_create);

        List<String> locations = new ArrayList<>();
        locations.add("Hà Nội");
        locations.add("TP.HCM");
        locations.add("Đà Nẵng");
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Fetch destinations dynamic
        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getPlaceApi()
                .getDestinations()
                .enqueue(new retrofit2.Callback<com.example.berotravel20.models.DestinationResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.berotravel20.models.DestinationResponse> call,
                            retrofit2.Response<com.example.berotravel20.models.DestinationResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            if (response.body().data != null && !response.body().data.isEmpty()) {
                                locations.clear();
                                locations.addAll(response.body().data);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.berotravel20.models.DestinationResponse> call,
                            Throwable t) {
                        // Keep default values on failure
                    }
                });

        tvStart.setOnClickListener(v -> showDatePicker(tvStart));
        tvEnd.setOnClickListener(v -> showDatePicker(tvEnd));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String selectedLoc = spinner.getSelectedItem().toString();
            String start = tvStart.getText().toString();
            String end = tvEnd.getText().toString();

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
                return;
            }

            createJourney(selectedLoc, start, end);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePicker(android.widget.TextView view) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(getContext(), (picker, y, m, d) -> {
            view.setText(d + "/" + (m + 1) + "/" + y);
        }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH))
                .show();
    }

    private void createJourney(String location, String start, String end) {
        // Empty places list for new trip
        List<String> places = new ArrayList<>();

        com.example.berotravel20.models.CreateJourneyRequest request = new com.example.berotravel20.models.CreateJourneyRequest(
                location, convertDate(start), convertDate(end), places);

        com.example.berotravel20.data.remote.RetrofitClient.getInstance(getContext())
                .getJourneyApi()
                .createJourney(request)
                .enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call,
                            retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã tạo chuyến đi!", Toast.LENGTH_SHORT).show();
                            fetchJourneys();
                        } else {
                            try {
                                String error = response.errorBody().string();
                                org.json.JSONObject json = new org.json.JSONObject(error);
                                Toast.makeText(getContext(), json.optString("message", "Lỗi tạo chuyến đi"),
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Lỗi tạo chuyến đi", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String convertDate(String date) {
        try {
            String[] parts = date.split("/");
            if (parts.length == 3)
                return parts[2] + "-" + parts[1] + "-" + parts[0];
        } catch (Exception e) {
        }
        return date;
    }
}