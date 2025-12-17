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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHelper {
    private GoogleMap googleMap;
    private Context context; // Context để convert Vector -> Bitmap

    // Quản lý các đối tượng vẽ trên map
    private Circle currentCircle;
    private Polyline currentPolyline;
    private Marker userNavMarker;

    // Map lưu trữ Marker để xử lý sự kiện click (Key: MarkerId, Value: Place)
    private Map<String, Place> markerMap = new HashMap<>();

    // List lưu trữ các marker object thực tế để xóa thủ công nếu cần
    private List<Marker> currentMarkers = new ArrayList<>();

    private static final int PRIMARY_COLOR = Color.parseColor("#007A8C");
    private static final int FILL_COLOR = Color.argb(34, 0, 122, 140);

    // Constructor nhận Context
    public MapHelper(Context context) {
        this.context = context;
    }

    // --- SETUP MAP ---
    public void setGoogleMap(GoogleMap map) {
        this.googleMap = map;
        if (this.googleMap != null) {
            // Tắt các nút mặc định của Google để tự custom giao diện
            this.googleMap.getUiSettings().setZoomControlsEnabled(false);
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            this.googleMap.getUiSettings().setCompassEnabled(false);
            this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        }
    }

    public void enableMyLocationLayer() {
        if (googleMap != null) {
            try {
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void disableMyLocationLayer() {
        if (googleMap != null) {
            try {
                googleMap.setMyLocationEnabled(false);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void moveCamera(LatLng latLng, float zoom) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    // --- NAVIGATION MODE (Chế độ dẫn đường) ---

    // Cập nhật vị trí mũi tên điều hướng và camera
    public void updateNavigationCamera(Location location) {
        if (googleMap == null) return;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Vẽ hoặc cập nhật mũi tên định vị
        if (userNavMarker == null) {
            // Lưu ý: R.drawable.ic_navigation_arrow cần là vector hoặc png hình mũi tên
            userNavMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(getBitmapFromVector(R.drawable.ic_navigation_arrow))
                    .anchor(0.5f, 0.5f) // Tâm ở giữa icon
                    .flat(true));       // Icon nằm bẹt xuống mặt đất xoay theo bản đồ
        } else {
            userNavMarker.setPosition(latLng);
            userNavMarker.setRotation(location.getBearing()); // Xoay mũi tên theo hướng đi
        }

        // Camera đi theo người dùng (zoom sâu, nghiêng 60 độ)
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(19f)
                .bearing(location.getBearing()) // Xoay bản đồ theo hướng đi
                .tilt(60)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
    }

    // Thoát chế độ dẫn đường, reset camera về góc nhìn từ trên xuống
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
                    .bearing(0) // Xoay về hướng Bắc
                    .tilt(0)    // Nhìn thẳng từ trên xuống
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(reset));
        }
    }

    // --- MARKERS & PLACES ---

    // Hiển thị danh sách địa điểm tìm được
    public void showMarkers(List<Place> places) {
        if (googleMap == null) return;

        // 1. Giữ lại Circle và Polyline nếu đang có
        // (Cách đơn giản nhất là clear hết rồi vẽ lại, nhưng để tối ưu thì chỉ xóa markers)
        clearMarkers();

        // 2. Vẽ marker mới
        if (places != null) {
            for (Place place : places) {
                LatLng loc = new LatLng(place.latitude, place.longitude);
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title(place.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                if (marker != null) {
                    markerMap.put(marker.getId(), place);
                    currentMarkers.add(marker);
                }
            }
        }
    }

    // Xóa tất cả marker địa điểm (nhưng giữ lại đường đi và vòng tròn tìm kiếm)
    public void clearMarkers() {
        for (Marker m : currentMarkers) {
            m.remove();
        }
        currentMarkers.clear();
        markerMap.clear();
    }

    // Thêm 1 marker điểm đến duy nhất (Dùng cho chế độ chỉ đường)
    public void addDestinationMarker(Place place) {
        if (googleMap == null) return;

        LatLng loc = new LatLng(place.latitude, place.longitude);
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(loc)
                .title(place.name)
                // Dùng màu khác để nổi bật (ví dụ màu Xanh)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if (marker != null) {
            markerMap.put(marker.getId(), place);
            currentMarkers.add(marker);
        }
    }

    public Place getPlaceByMarkerId(String markerId) {
        return markerMap.get(markerId);
    }

    // --- SHAPES (Circle & Polyline) ---

    public void drawSearchRadius(LatLng center, int radiusKm) {
        if (googleMap == null) return;
        clearPolyline(); // Xóa đường đi cũ nếu có
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

        // Zoom camera để thấy toàn bộ đường đi
        if (!path.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng p : path) builder.include(p);
            try {
                // Padding 150px để đường không bị sát mép màn hình
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
        currentMarkers.clear();
        currentCircle = null;
        currentPolyline = null;
    }

    // --- UTILS ---

    // Chuyển Vector Drawable (XML) thành BitmapDescriptor để dùng cho Marker Icon
    private BitmapDescriptor getBitmapFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return null;

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private float getZoomLevelForRadius(int radiusKm) {
        if (radiusKm <= 1) return 14f;
        if (radiusKm <= 5) return 12.5f;
        if (radiusKm <= 10) return 11.5f;
        if (radiusKm <= 20) return 10.5f;
        return 9f;
    }
}