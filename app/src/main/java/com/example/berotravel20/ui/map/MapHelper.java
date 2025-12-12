package com.example.berotravel20.ui.map;

import android.graphics.Color;
import com.example.berotravel20.data.model.Place.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHelper {
    private GoogleMap googleMap;
    private Circle currentCircle;
    private Polyline currentPolyline;
    private Map<String, Place> markerMap = new HashMap<>();

    public void setGoogleMap(GoogleMap map) {
        this.googleMap = map;
        if (this.googleMap != null) {
            this.googleMap.getUiSettings().setZoomControlsEnabled(false);
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    public void moveCamera(LatLng latLng, float zoom) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    public void showMarkers(List<Place> places) {
        if (googleMap == null) return;

        // Lưu lại thông tin hình vẽ cũ trước khi clear
        LatLng circleCenter = (currentCircle != null) ? currentCircle.getCenter() : null;
        double circleRadius = (currentCircle != null) ? currentCircle.getRadius() : 0;
        List<LatLng> polylinePoints = (currentPolyline != null) ? currentPolyline.getPoints() : null;

        googleMap.clear();

        // Vẽ lại hình cũ (nếu có)
        if (circleCenter != null) drawCircle(circleCenter, circleRadius);
        if (polylinePoints != null) drawPolyline(polylinePoints);

        markerMap.clear();
        if (places != null) {
            for (Place place : places) {
                LatLng loc = new LatLng(place.latitude, place.longitude);
                Marker marker = googleMap.addMarker(new MarkerOptions().position(loc).title(place.name));
                if (marker != null) {
                    markerMap.put(marker.getId(), place);
                }
            }
        }
    }

    public Place getPlaceByMarkerId(String markerId) {
        return markerMap.get(markerId);
    }

    // Hàm public dùng để gọi từ Activity (truyền km)
    public void drawSearchRadius(LatLng center, int radiusKm) {
        if (googleMap == null) return;

        clearPolyline(); // Xóa đường đi cho đỡ rối

        // Gọi hàm private bên dưới để vẽ
        drawCircle(center, radiusKm * 1000);

        float zoomLevel = getZoomLevelForRadius(radiusKm);
        moveCamera(center, zoomLevel);
    }

    // [FIX LỖI] Thêm hàm private này để vẽ thực tế (truyền mét)
    private void drawCircle(LatLng center, double radiusMeters) {
        if (googleMap == null) return;

        if (currentCircle != null) currentCircle.remove();

        currentCircle = googleMap.addCircle(new CircleOptions()
                .center(center)
                .radius(radiusMeters)
                .strokeWidth(2f)
                .strokeColor(Color.parseColor("#4285F4"))
                .fillColor(Color.parseColor("#224285F4")));
    }

    public void drawPolyline(List<LatLng> path) {
        if (googleMap == null) return;
        clearPolyline();

        PolylineOptions opts = new PolylineOptions()
                .addAll(path)
                .color(Color.BLUE)
                .width(15)
                .geodesic(true);
        currentPolyline = googleMap.addPolyline(opts);

        if (!path.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng p : path) builder.include(p);
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearPolyline() {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }
    }

    public void clearAll() {
        if (googleMap != null) googleMap.clear();
        markerMap.clear();
        currentCircle = null;
        currentPolyline = null;
    }

    private float getZoomLevelForRadius(int radiusKm) {
        if (radiusKm <= 1) return 14f;
        if (radiusKm <= 5) return 12.5f;
        if (radiusKm <= 10) return 11.5f;
        if (radiusKm <= 20) return 10.5f;
        return 9f;
    }
}