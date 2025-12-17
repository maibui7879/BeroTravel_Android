package com.example.berotravel20.ui.main.place;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.berotravel20.R;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<String> imageUrls = new ArrayList<>();
    private OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(int position);
    }

    public PhotoAdapter(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<String> newUrls) {
        this.imageUrls = (newUrls != null) ? newUrls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(imageUrls.get(position))
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imgPhoto);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPhotoClick(position);
        });
    }

    @Override
    public int getItemCount() { return imageUrls.size(); }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.img_photo);
        }
    }
}