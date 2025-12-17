package com.example.berotravel20.ui.main.place;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.berotravel20.R;

import java.util.ArrayList;

public class PhotoGalleryFragment extends Fragment {

    private static final String ARG_IMAGE_URLS = "image_urls";
    private static final String ARG_START_POS = "start_pos";

    private ArrayList<String> imageUrls;
    private int startPos;

    public PhotoGalleryFragment() {
        // Required empty public constructor
    }

    public static PhotoGalleryFragment newInstance(ArrayList<String> imageUrls, int startPos) {
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMAGE_URLS, imageUrls);
        args.putInt(ARG_START_POS, startPos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrls = getArguments().getStringArrayList(ARG_IMAGE_URLS);
            startPos = getArguments().getInt(ARG_START_POS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        ImageButton btnClose = view.findViewById(R.id.btn_close);
        TextView tvIndicator = view.findViewById(R.id.tv_indicator);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            GalleryAdapter adapter = new GalleryAdapter(imageUrls);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(startPos, false);

            updateIndicator(tvIndicator, startPos, imageUrls.size());

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateIndicator(tvIndicator, position, imageUrls.size());
                }
            });
        }

        btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void updateIndicator(TextView tv, int position, int total) {
        tv.setText((position + 1) + "/" + total);
    }
}
