package com.example.berotravel20.ui.map;

import android.location.Location;
import android.util.Log;

import com.example.berotravel20.data.model.ORS.Step;
import com.example.berotravel20.utils.MapUtils;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class MapNavigationManager {

    private List<Step> steps;
    private List<LatLng> fullPath;
    private int currentStepIndex = 0;

    // [CẤU HÌNH]
    private static final float REROUTE_DISTANCE_THRESHOLD = 50f; // Lệch 50m -> Reroute
    private static final float WRONG_WAY_ANGLE_THRESHOLD = 120f; // Lệch 120 độ -> Đi ngược chiều
    private static final float MIN_SPEED_TO_CHECK_BEARING = 1.5f; // Phải di chuyển > 1.5 m/s (~5km/h) mới check hướng

    public interface NavigationListener {
        void onUpdateInstruction(String instruction, String distanceText);
        void onNextStep(String nextInstruction);
        void onArrived();
        void onRerouteNeeded();
    }

    private final NavigationListener listener;

    public MapNavigationManager(NavigationListener listener) {
        this.listener = listener;
    }

    public void startNewRoute(List<Step> steps, List<LatLng> fullPath) {
        this.steps = steps;
        this.fullPath = fullPath;
        this.currentStepIndex = 0;
        if (steps != null && !steps.isEmpty()) {
            updateProgress(null);
        }
    }

    public void onLocationUpdated(Location userLocation) {
        if (steps == null || fullPath == null || currentStepIndex >= steps.size()) return;

        updateProgress(userLocation);

        // Kiểm tra lệch đường HOẶC đi ngược chiều
        checkRouteIntegrity(userLocation);
    }

    private void updateProgress(Location userLocation) {
        Step currentStep = steps.get(currentStepIndex);
        int endIndex = currentStep.way_points[1];
        if (endIndex >= fullPath.size()) endIndex = fullPath.size() - 1;
        LatLng targetPoint = fullPath.get(endIndex);

        float distanceToNextStep = 0;
        if (userLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    targetPoint.latitude, targetPoint.longitude,
                    results
            );
            distanceToNextStep = results[0];
        }

        String distText = (distanceToNextStep < 1000) ? (int) distanceToNextStep + " m" : String.format("%.1f km", distanceToNextStep / 1000);
        listener.onUpdateInstruction(MapUtils.cleanInstruction(currentStep.instruction), distText);

        if (userLocation != null && distanceToNextStep < 20) {
            currentStepIndex++;
            if (currentStepIndex < steps.size()) {
                String nextRaw = steps.get(currentStepIndex).instruction;
                listener.onNextStep(MapUtils.cleanInstruction(nextRaw));
                updateProgress(userLocation);
            } else {
                listener.onArrived();
            }
        }
    }

    // [LOGIC MỚI] Gộp kiểm tra khoảng cách và hướng đi
    private void checkRouteIntegrity(Location userLocation) {
        // 1. Kiểm tra lệch đường (Distance Check)
        float minDistance = Float.MAX_VALUE;
        int startScan = Math.max(0, currentStepIndex * 2);
        // Quét 20 điểm gần nhất thôi để tối ưu
        int endScan = Math.min(fullPath.size(), startScan + 20);

        // Tìm điểm gần nhất trên đường đi
        int closestPointIndex = -1;

        for (int i = startScan; i < endScan; i++) {
            LatLng point = fullPath.get(i);
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), point.latitude, point.longitude, results);
            if (results[0] < minDistance) {
                minDistance = results[0];
                closestPointIndex = i;
            }
        }

        // Nếu lệch quá xa -> Reroute
        if (minDistance > REROUTE_DISTANCE_THRESHOLD) {
            Log.d("NAV", "Lệch đường quá " + minDistance + "m -> Reroute");
            listener.onRerouteNeeded();
            return; // Đã sai đường rồi thì ko cần check ngược chiều nữa
        }

        // 2. Kiểm tra đi ngược chiều (Wrong Way Check)
        // Chỉ kiểm tra khi user đang di chuyển và tìm thấy điểm gần nhất
        if (userLocation.hasSpeed() && userLocation.getSpeed() > MIN_SPEED_TO_CHECK_BEARING && closestPointIndex != -1 && closestPointIndex < fullPath.size() - 1) {

            // Tính hướng của con đường tại điểm gần nhất
            LatLng p1 = fullPath.get(closestPointIndex);
            LatLng p2 = fullPath.get(closestPointIndex + 1);
            float roadBearing = calculateBearing(p1, p2);
            float userBearing = userLocation.getBearing();

            // Tính độ lệch góc
            float angleDiff = Math.abs(userBearing - roadBearing);
            if (angleDiff > 180) angleDiff = 360 - angleDiff; // Chuẩn hóa góc (ví dụ 350 vs 10 độ là lệch 20 độ)

            // Nếu lệch > 120 độ (Đi ngược) -> Reroute
            if (angleDiff > WRONG_WAY_ANGLE_THRESHOLD) {
                Log.d("NAV", "Đi ngược chiều! Góc lệch: " + angleDiff + " -> Reroute");
                listener.onRerouteNeeded();
            }
        }
    }

    private float calculateBearing(LatLng start, LatLng end) {
        Location locStart = new Location("A");
        locStart.setLatitude(start.latitude);
        locStart.setLongitude(start.longitude);

        Location locEnd = new Location("B");
        locEnd.setLatitude(end.latitude);
        locEnd.setLongitude(end.longitude);

        return locStart.bearingTo(locEnd);
    }
}