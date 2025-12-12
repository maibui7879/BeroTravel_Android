package com.example.berotravel20.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public interface LocationCallback {
        void onLocationFound(Location location);
    }

    public LocationHelper(Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void getLastLocation(LocationCallback callback) {
        if (!hasPermission()) {
            requestPermission();
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location != null) {
                    callback.onLocationFound(location);
                } else {
                    requestCurrentLocation(callback);
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void requestCurrentLocation(LocationCallback callback) {
        if (!hasPermission()) return;

        CancellationTokenSource tokenSource = new CancellationTokenSource();
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken())
                    .addOnSuccessListener(activity, location -> {
                        if (location != null) {
                            callback.onLocationFound(location);
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }
}