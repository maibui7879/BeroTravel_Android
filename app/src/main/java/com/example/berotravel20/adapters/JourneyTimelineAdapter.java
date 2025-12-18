package com.example.berotravel20.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Journey.Journey;
import java.util.List;

public class JourneyTimelineAdapter extends RecyclerView.Adapter<JourneyTimelineAdapter.TimelineViewHolder> {

    private List<Journey.JourneyPlace> places;

    public JourneyTimelineAdapter(List<Journey.JourneyPlace> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_horizontal, parent, false);
        return new TimelineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        Journey.JourneyPlace item = places.get(position);

        // Set tên địa điểm
        if (item.place != null) {
            holder.tvName.setText(item.place.name);
        } else {
            holder.tvName.setText("Điểm " + (position + 1));
        }

        // LOGIC QUAN TRỌNG: Ẩn đường kẻ nếu là phần tử cuối cùng
        if (position == places.size() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }

        // (Optional) Logic màu sắc: Nếu đã đi qua thì màu xanh, chưa đi thì màu xám...
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        View dot, line;
        TextView tvName;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.view_dot);
            line = itemView.findViewById(R.id.view_line);
            tvName = itemView.findViewById(R.id.tv_place_name);
        }
    }
}