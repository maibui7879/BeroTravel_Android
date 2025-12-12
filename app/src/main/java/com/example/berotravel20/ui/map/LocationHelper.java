package com.example.berotravel20.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isRequestingUpdates = false;

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public interface MyLocationListener {
        void onLocationChanged(Location location);
    }

    public LocationHelper(Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    // 1. Lấy vị trí 1 lần (Cho lúc mới vào app)
    public void getLastLocation(MyLocationListener listener) {
        if (!hasPermission()) {
            requestPermission();
            return;
        }
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location != null) {
                    listener.onLocationChanged(location);
                } else {
                    // Nếu null thì thử request update 1 lần
                    startLocationUpdates(listener);
                }
            });
        } catch (SecurityException e) { e.printStackTrace(); }
    }

    // 2. Bắt đầu theo dõi vị trí liên tục (Cho chế độ dẫn đường)
    public void startLocationUpdates(MyLocationListener listener) {
        if (!hasPermission() || isRequestingUpdates) return;

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000) // Cập nhật mỗi 2 giây
                .setMinUpdateDistanceMeters(5) // Hoặc khi di chuyển 5 mét
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    listener.onLocationChanged(location);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            isRequestingUpdates = true;
        } catch (SecurityException e) { e.printStackTrace(); }
    }

    // 3. Dừng theo dõi
    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isRequestingUpdates = false;
        }
    }

    public boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }
}