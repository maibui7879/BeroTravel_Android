package com.example.berotravel20.ui.main.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.berotravel20.R;
import com.example.berotravel20.models.PlaceResponse;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private androidx.recyclerview.widget.RecyclerView rvPopularPlaces, rvHotels, rvHomestays;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private android.widget.TextView tvWelcome;
    private com.example.berotravel20.network.ApiService apiService;
    private int runningTasks = 0;

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view instanceof androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
            swipeRefreshLayout = (androidx.swiperefreshlayout.widget.SwipeRefreshLayout) view;
        } else {
            swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        }

        rvPopularPlaces = view.findViewById(R.id.rv_popular_places);
        rvHotels = view.findViewById(R.id.rv_hotels);
        rvHomestays = view.findViewById(R.id.rv_homestays);
        tvWelcome = view.findViewById(R.id.tv_welcome);

        setupRecyclerView(rvPopularPlaces);
        setupRecyclerView(rvHotels);
        setupRecyclerView(rvHomestays);

        apiService = com.example.berotravel20.network.ApiClient.getClient(getContext())
                .create(com.example.berotravel20.network.ApiService.class);

        // Fetch initial data
        loadData();

        swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void setupRecyclerView(androidx.recyclerview.widget.RecyclerView rv) {
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(),
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);
        runningTasks = 4; // Places, Hotels, Homestays, Profile
        fetchPlaces(); // Popular (mixed)
        fetchHotels();
        fetchHomestays();
        fetchUserInfo();
    }

    private void checkRefreshState() {
        runningTasks--;
        if (runningTasks <= 0) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void fetchUserInfo() {
        apiService.getProfile().enqueue(new retrofit2.Callback<com.example.berotravel20.models.UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.berotravel20.models.UserResponse> call,
                    retrofit2.Response<com.example.berotravel20.models.UserResponse> response) {
                checkRefreshState();
                if (response.isSuccessful() && response.body() != null) {
                    if (tvWelcome != null) {
                        tvWelcome.setText("Welcome Back, " + response.body().name + "!");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.berotravel20.models.UserResponse> call, Throwable t) {
                checkRefreshState();
                android.util.Log.e("HomeFragment", "Error fetching profile", t);
            }
        });
    }

    /* --- ADDED FOR SCHEDULE FEATURE --- */
    private List<PlaceResponse.Place> filterPlacesByLocation(List<PlaceResponse.Place> places) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("BeroTravelPrefs",
                android.content.Context.MODE_PRIVATE);
        String currentTripLocation = prefs.getString("CURRENT_TRIP_LOCATION", null);

        if (currentTripLocation == null || currentTripLocation.isEmpty()) {
            return places;
        }

        List<PlaceResponse.Place> filteredList = new java.util.ArrayList<>();
        for (PlaceResponse.Place place : places) {
            // Simple string matching. Modify if backend provides explicit city field.
            if (place.address != null && place.address.contains(currentTripLocation)) {
                filteredList.add(place);
            }
        }
        return filteredList;
    }
    /* ---------------------------------- */

    private void fetchPlaces() {
        // Fetch mixed/popular places (no category filter)
        apiService.getPlaces(50, null).enqueue(new retrofit2.Callback<com.example.berotravel20.models.PlaceResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                    retrofit2.Response<com.example.berotravel20.models.PlaceResponse> response) {
                checkRefreshState();
                if (response.isSuccessful() && response.body() != null) {
                    List<PlaceResponse.Place> allPlaces = response.body().data;
                    List<PlaceResponse.Place> filteredPlaces = filterPlacesByLocation(allPlaces);
                    HomePlaceAdapter adapter = new HomePlaceAdapter(filteredPlaces, this::navigateToPlace);
                    rvPopularPlaces.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call, Throwable t) {
                checkRefreshState();
                android.util.Log.e("HomeFragment", "Error fetching popular places", t);
            }

            private void navigateToPlace(com.example.berotravel20.models.PlaceResponse.Place place) {
                com.example.berotravel20.ui.main.place.PlaceFragment placeFragment = com.example.berotravel20.ui.main.place.PlaceFragment
                        .newInstance(place._id);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.base_container, placeFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void fetchHotels() {
        apiService.getPlaces(50, "Hotel")
                .enqueue(new retrofit2.Callback<com.example.berotravel20.models.PlaceResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                            retrofit2.Response<com.example.berotravel20.models.PlaceResponse> response) {
                        checkRefreshState();
                        if (response.isSuccessful() && response.body() != null) {
                            List<PlaceResponse.Place> allHotels = response.body().data;
                            List<PlaceResponse.Place> filteredHotels = filterPlacesByLocation(allHotels);
                            HomePlaceAdapter adapter = new HomePlaceAdapter(filteredHotels,
                                    this::navigateToPlace);
                            rvHotels.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                            Throwable t) {
                        checkRefreshState();
                        android.util.Log.e("HomeFragment", "Error fetching hotels", t);
                    }

                    private void navigateToPlace(com.example.berotravel20.models.PlaceResponse.Place place) {
                        com.example.berotravel20.ui.main.place.PlaceFragment placeFragment = com.example.berotravel20.ui.main.place.PlaceFragment
                                .newInstance(place._id);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.base_container, placeFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });
    }

    private void fetchHomestays() {
        apiService.getPlaces(50, "Homestay")
                .enqueue(new retrofit2.Callback<com.example.berotravel20.models.PlaceResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                            retrofit2.Response<com.example.berotravel20.models.PlaceResponse> response) {
                        checkRefreshState();
                        if (response.isSuccessful() && response.body() != null) {
                            List<PlaceResponse.Place> allHomestays = response.body().data;
                            List<PlaceResponse.Place> filteredHomestays = filterPlacesByLocation(allHomestays);
                            HomePlaceAdapter adapter = new HomePlaceAdapter(filteredHomestays,
                                    this::navigateToPlace);
                            rvHomestays.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.berotravel20.models.PlaceResponse> call,
                            Throwable t) {
                        checkRefreshState();
                        android.util.Log.e("HomeFragment", "Error fetching homestays", t);
                    }

                    private void navigateToPlace(com.example.berotravel20.models.PlaceResponse.Place place) {
                        com.example.berotravel20.ui.main.place.PlaceFragment placeFragment = com.example.berotravel20.ui.main.place.PlaceFragment
                                .newInstance(place._id);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.base_container, placeFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });
    }
}