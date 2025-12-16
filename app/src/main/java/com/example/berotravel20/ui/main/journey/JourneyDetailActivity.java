package com.example.berotravel20.ui.main.journey;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Journey.Journey;
import com.example.berotravel20.models.SingleJourneyResponse;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JourneyDetailActivity extends AppCompatActivity implements JourneyPlaceAdapter.OnItemClickListener {

    private RecyclerView rvPlaces;
    private TextView tvTitle;
    private JourneyPlaceAdapter adapter;
    private List<Map<String, String>> placeList;
    private String journeyId;
    private Journey currentJourney;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_detail);

        journeyId = getIntent().getStringExtra("JOURNEY_ID");
        if (journeyId == null) {
            Toast.makeText(this, "Không tìm thấy ID chuyến đi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tv_detail_title);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        rvPlaces = findViewById(R.id.rv_journey_places);
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));

        placeList = new ArrayList<>();
        adapter = new JourneyPlaceAdapter(placeList, this);
        rvPlaces.setAdapter(adapter);

        fetchJourneyDetails();
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchJourneyDetails() {
        com.example.berotravel20.data.remote.RetrofitClient.getInstance(this)
                .getJourneyApi()
                .getJourneyById(journeyId)
                .enqueue(new Callback<SingleJourneyResponse>() {
                    @Override
                    public void onResponse(Call<SingleJourneyResponse> call, Response<SingleJourneyResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentJourney = response.body().data;
                            updateUI();
                        } else {
                            Toast.makeText(JourneyDetailActivity.this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SingleJourneyResponse> call, Throwable t) {
                        Toast.makeText(JourneyDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (currentJourney == null)
            return;

        tvTitle.setText(currentJourney.location != null ? currentJourney.location : "Chi tiết chuyến đi");

        placeList.clear();
        if (currentJourney.places != null) {
            for (Journey.JourneyPlaceWithStatus p : currentJourney.places) {
                if (p.place != null) {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", p.place.id);
                    map.put("name", p.place.name);
                    map.put("address", p.place.address);
                    map.put("image_url", p.place.image_url);
                    map.put("startTime", p.startTime);
                    map.put("endTime", p.endTime);
                    placeList.add(map);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteClick(int position) {
        if (placeList.isEmpty() || position >= placeList.size())
            return;

        String placeId = placeList.get(position).get("id");
        if (placeId == null)
            return;

        com.example.berotravel20.data.remote.RetrofitClient.getInstance(this)
                .getJourneyApi()
                .removePlaceFromJourney(journeyId, placeId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(JourneyDetailActivity.this, "Đã xóa địa điểm", Toast.LENGTH_SHORT).show();
                            fetchJourneyDetails(); // Refresh
                        } else {
                            Toast.makeText(JourneyDetailActivity.this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(JourneyDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
