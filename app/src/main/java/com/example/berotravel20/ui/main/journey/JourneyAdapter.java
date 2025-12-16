package com.example.berotravel20.ui.main.journey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Journey.Journey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    private List<Journey> journeyList;
    private Context context;
    private OnDeleteClickListener deleteListener;
    private OnItemClickListener itemClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String journeyId);
    }

    public interface OnItemClickListener {
        void onItemClick(String journeyId);
    }

    public JourneyAdapter(Context context, List<Journey> journeyList, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.journeyList = journeyList;
        this.deleteListener = deleteListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_journey, parent, false);
        return new JourneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyViewHolder holder, int position) {
        Journey journey = journeyList.get(position);

        String location = journey.location;
        if (location == null || location.isEmpty()) {
            location = "Chuyến đi chưa đặt tên";
        }
        holder.tvLocation.setText(location);

        String dates = "";
        if (journey.startDate != null && journey.endDate != null) {
            dates = formatDate(journey.startDate) + " - " + formatDate(journey.endDate);
        } else {
            dates = "Chưa có thời gian";
        }
        holder.tvDates.setText(dates);

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(journey.id);
            }
        });

        // 30 Minute Delete Rule
        if (canDelete(journey.createdAt)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null)
                    deleteListener.onDeleteClick(journey.id); // Assuming model has id or _id
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return journeyList.size();
    }

    private boolean canDelete(String createdAtStr) {
        if (createdAtStr == null)
            return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date createdDate = sdf.parse(createdAtStr);
            if (createdDate != null) {
                long diff = System.currentTimeMillis() - createdDate.getTime();
                return diff <= 30 * 60 * 1000;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String formatDate(String dateStr) {
        if (dateStr == null)
            return "";
        try {
            if (dateStr.contains("T"))
                return dateStr.split("T")[0];
        } catch (Exception e) {
        }
        return dateStr;
    }

    public static class JourneyViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvDates;
        Button btnDelete;

        public JourneyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tv_journey_location);
            tvDates = itemView.findViewById(R.id.tv_journey_dates);
            btnDelete = itemView.findViewById(R.id.btn_delete_journey);
        }
    }
}
