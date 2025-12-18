package com.example.berotravel20.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Journey.Journey;

import java.util.List;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    private Context context;
    private List<Journey> listJourney;
    private OnJourneyClickListener listener;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public interface OnJourneyClickListener {
        void onClick(Journey journey);
        void onDelete(Journey journey);
    }

    public JourneyAdapter(Context context, List<Journey> listJourney, OnJourneyClickListener listener) {
        this.context = context;
        this.listJourney = listJourney;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journey, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyViewHolder holder, int position) {
        Journey journey = listJourney.get(position);
        if (journey == null) return;

        // 1. Logic sinh tên từ tối đa 3 địa điểm
        String generatedName = generateJourneyName(journey);
        holder.tvName.setText(generatedName);

        // 2. Logic đếm tổng số địa điểm
        int totalPlaces = (journey.places != null) ? journey.places.size() : 0;
        holder.tvCount.setText(totalPlaces + " địa điểm");

        // 3. Xử lý trạng thái
        String status = journey.status != null ? journey.status.toUpperCase() : "ONGOING";
        holder.tvStatus.setText(status);
        if (status.equals("ONGOING")) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
        }

        // 4. Setup Xương cá ngang (Timeline)
        if (journey.places != null && !journey.places.isEmpty()) {
            holder.rvTimeline.setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    holder.rvTimeline.getContext(), LinearLayoutManager.HORIZONTAL, false);

            // Tối ưu hóa render cho nested recyclerview
            layoutManager.setInitialPrefetchItemCount(journey.places.size());
            holder.rvTimeline.setLayoutManager(layoutManager);
            holder.rvTimeline.setRecycledViewPool(viewPool);

            JourneyTimelineAdapter timelineAdapter = new JourneyTimelineAdapter(journey.places);
            holder.rvTimeline.setAdapter(timelineAdapter);
        } else {
            holder.rvTimeline.setVisibility(View.GONE);
        }

        // 5. Sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(journey);
        });

        // 6. Hỗ trợ xóa bằng Long Click
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(journey);
            return true;
        });
    }

    /**
     * Hàm helper tạo tên: Lấy tên của tối đa 3 địa điểm đầu tiên nối lại
     */
    private String generateJourneyName(Journey journey) {
        if (journey.places == null || journey.places.isEmpty()) {
            return "Hành trình mới";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Journey.JourneyPlace jp : journey.places) {
            if (jp.place != null && jp.place.name != null) {
                if (count > 0) sb.append(" - ");
                sb.append(jp.place.name);
                count++;
            }
            if (count == 3) break; // Dừng lại ở địa điểm thứ 3
        }

        String result = sb.toString();
        return result.isEmpty() ? "Hành trình không tên" : result;
    }

    @Override
    public int getItemCount() {
        return listJourney != null ? listJourney.size() : 0;
    }

    public static class JourneyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvCount;
        RecyclerView rvTimeline;

        public JourneyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bạn hãy kiểm tra ID trong file item_journey.xml để khớp với các dòng dưới đây
            tvName = itemView.findViewById(R.id.tv_journey_id);
            tvStatus = itemView.findViewById(R.id.tv_journey_status);
            tvCount = itemView.findViewById(R.id.tv_journey_places_count); // ID mới để hiển thị số lượng
            rvTimeline = itemView.findViewById(R.id.rv_timeline_preview);
        }
    }
}