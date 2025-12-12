package com.example.berotravel20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.ORS.Step;

import java.util.ArrayList;
import java.util.List;

public class DirectionStepAdapter extends RecyclerView.Adapter<DirectionStepAdapter.StepViewHolder> {

    private List<Step> steps = new ArrayList<>();
    private Context context;

    public void setData(List<Step> steps) {
        this.steps = steps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_direction_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = steps.get(position);

        // [FIX LỖI LẶP TỪ] Sử dụng hàm cleanInstruction thay vì chỉ viết hoa
        String cleanedInstruction = cleanInstruction(step.instruction);
        holder.tvInstruction.setText(cleanedInstruction);

        // 2. Format khoảng cách
        if (step.distance < 1000) {
            holder.tvStepDistance.setText((int) step.distance + " m");
        } else {
            holder.tvStepDistance.setText(String.format("%.1f km", step.distance / 1000));
        }

        // 3. Chọn Icon
        int iconRes = getIconForStepType(step.type);
        holder.imgIcon.setImageResource(iconRes);

        // --- MÀU SẮC ---
        holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryBackground));
        holder.tvStepDistance.setTextColor(ContextCompat.getColor(context, R.color.colorGeneralText));
        holder.tvInstruction.setTextColor(ContextCompat.getColor(context, R.color.black));
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    // [HÀM MỚI] Xử lý chuỗi để xóa từ lặp và viết hoa
    private String cleanInstruction(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        String s = raw.trim();

        // Xử lý các lỗi lặp từ thường gặp từ API (không phân biệt hoa thường)
        // (?i) là cờ case-insensitive, ^ là bắt đầu chuỗi
        s = s.replaceAll("(?i)^Rẽ Rẽ", "Rẽ");
        s = s.replaceAll("(?i)^Đi Đi", "Đi");
        s = s.replaceAll("(?i)^Quay đầu Quay đầu", "Quay đầu");

        // Viết hoa chữ cái đầu tiên
        return capitalizeFirstLetter(s);
    }

    private String capitalizeFirstLetter(String original) {
        if (original == null || original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private int getIconForStepType(int type) {
        switch (type) {
            case 0: // Left
            case 2: // Sharp Left
            case 4: // Slight Left
                return R.drawable.ic_turn_left;
            case 1: // Right
            case 3: // Sharp Right
            case 5: // Slight Right
                return R.drawable.ic_turn_right;
            case 10: // Head
                return R.drawable.ic_arrow_up;
            case 11: // Roundabout
                return R.drawable.ic_roundabout;
            case 12: // Arrive
            default:
                return R.drawable.ic_search_gray;
        }
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstruction, tvStepDistance;
        ImageView imgIcon;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
            tvStepDistance = itemView.findViewById(R.id.tvStepDistance);
            imgIcon = itemView.findViewById(R.id.imgDirectionIcon);
        }
    }
}