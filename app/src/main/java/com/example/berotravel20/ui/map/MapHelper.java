package com.example.berotravel20.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.example.berotravel20.R;
import com.example.berotravel20.data.model.Place.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
    private Context context; // [MỚI] Cần context để load Vector
    private Circle currentCircle;
    private Polyline currentPolyline;
    private Marker userNavMarker;
    private Map<String, Place> markerMap = new HashMap<>();

    private static final int PRIMARY_COLOR = Color.parseColor("#007A8C");
    private static final int FILL_COLOR = Color.argb(34, 0, 122, 140);

    // [MỚI] Thêm constructor để nhận Context
    public MapHelper(Context context) {
        this.context = context;
    }

    // [MỚI] Helper chuyển Vector -> Bitmap
    private BitmapDescriptor getBitmapFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return null;

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void setGoogleMap(GoogleMap map) {
        this.googleMap = map;
        if (this.googleMap != null) {
            this.googleMap.getUiSettings().setZoomControlsEnabled(false);
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            this.googleMap.getUiSettings().setCompassEnabled(false);
        }
    }

    public void enableMyLocationLayer() {
        if (googleMap != null) {
            try { googleMap.setMyLocationEnabled(true); }
            catch (SecurityException e) { e.printStackTrace(); }
        }
    }

    public void disableMyLocationLayer() {
        if (googleMap != null) {
            try { googleMap.setMyLocationEnabled(false); }
            catch (SecurityException e) { e.printStackTrace(); }
        }
    }

    public void moveCamera(LatLng latLng, float zoom) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    // [SỬA LỖI Ở ĐÂY] Dùng hàm getBitmapFromVector
    public void updateNavigationCamera(Location location) {
        if (googleMap == null) return;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (userNavMarker == null) {
            userNavMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(getBitmapFromVector(R.drawable.ic_navigation_arrow)) // [FIXED]
                    .anchor(0.5f, 0.5f)
                    .flat(true));
        } else {
            userNavMarker.setPosition(latLng);
            userNavMarker.setRotation(location.getBearing());
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(19f)
                .bearing(location.getBearing())
                .tilt(60)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
    }

    // ... (Các phần còn lại giữ nguyên: stopNavigationMode, showMarkers, drawSearchRadius...)

    public void stopNavigationMode() {
        if (userNavMarker != null) {
            userNavMarker.remove();
            userNavMarker = null;
        }
        if (googleMap != null) {
            CameraPosition current = googleMap.getCameraPosition();
            CameraPosition reset = new CameraPosition.Builder()
                    .target(current.target)
                    .zoom(16f)
                    .bearing(0)
                    .tilt(0)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(reset));
        }
    }

    public void showMarkers(List<Place> places) {
        if (googleMap == null) return;
        LatLng circleCenter = (currentCircle != null) ? currentCircle.getCenter() : null;
        double circleRadius = (currentCircle != null) ? currentCircle.getRadius() : 0;
        List<LatLng> polylinePoints = (currentPolyline != null) ? currentPolyline.getPoints() : null;

        googleMap.clear();

        if (circleCenter != null) drawCircle(circleCenter, circleRadius);
        if (polylinePoints != null) drawPolyline(polylinePoints);

        markerMap.clear();
        if (places != null) {
            for (Place place : places) {
                LatLng loc = new LatLng(place.latitude, place.longitude);
                Marker marker = googleMap.addMarker(new MarkerOptions().position(loc).title(place.name));
                if (marker != null) markerMap.put(marker.getId(), place);
            }
        }
    }

    public Place getPlaceByMarkerId(String markerId) {
        return markerMap.get(markerId);
    }

    public void drawSearchRadius(LatLng center, int radiusKm) {
        if (googleMap == null) return;
        clearPolyline();
        drawCircle(center, radiusKm * 1000);
        moveCamera(center, getZoomLevelForRadius(radiusKm));
    }

    private void drawCircle(LatLng center, double radiusMeters) {
        if (googleMap == null) return;
        if (currentCircle != null) currentCircle.remove();
        currentCircle = googleMap.addCircle(new CircleOptions()
                .center(center)
                .radius(radiusMeters)
                .strokeWidth(3f)
                .strokeColor(PRIMARY_COLOR)
                .fillColor(FILL_COLOR));
    }

    public void drawPolyline(List<LatLng> path) {
        if (googleMap == null) return;
        clearPolyline();
        PolylineOptions opts = new PolylineOptions()
                .addAll(path)
                .color(PRIMARY_COLOR)
                .width(15)
                .geodesic(true);
        currentPolyline = googleMap.addPolyline(opts);

        if (!path.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng p : path) builder.include(p);
            try { googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150)); }
            catch (Exception e) { e.printStackTrace(); }
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