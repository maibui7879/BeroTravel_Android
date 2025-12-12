package com.example.berotravel20.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import để lấy màu
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

        // 1. Xử lý câu hướng dẫn
        String rawInstruction = step.instruction;
        holder.tvInstruction.setText(capitalizeFirstLetter(rawInstruction));

        // 2. Format khoảng cách
        if (step.distance < 1000) {
            holder.tvStepDistance.setText((int) step.distance + " m");
        } else {
            holder.tvStepDistance.setText(String.format("%.1f km", step.distance / 1000));
        }

        // 3. Chọn Icon dựa trên loại chỉ dẫn
        int iconRes = getIconForStepType(step.type);
        holder.imgIcon.setImageResource(iconRes);

        // --- [CẬP NHẬT MÀU SẮC] ---

        // Icon chỉ hướng: Tô màu chủ đạo (Primary Background - #007A8C)
        holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryBackground));

        // Khoảng cách: Tô màu xám (General Text - #6C757D)
        holder.tvStepDistance.setTextColor(ContextCompat.getColor(context, R.color.colorGeneralText));

        // Câu lệnh: Màu đen cho rõ ràng
        holder.tvInstruction.setTextColor(ContextCompat.getColor(context, R.color.black));
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    // Hàm viết hoa chữ cái đầu
    private String capitalizeFirstLetter(String original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    // Hàm map icon
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
            case 13:
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