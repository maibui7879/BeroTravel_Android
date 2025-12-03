package com.example.berotravel20.data.repository;

import com.example.berotravel20.data.api.PlaceStatusApiService;
import com.example.berotravel20.data.common.DataCallback;
import com.example.berotravel20.data.model.PlaceStatus.PlaceStatus;
import com.example.berotravel20.data.model.Place.PlaceStatusRequest;
import com.example.berotravel20.data.remote.RetrofitClient;

public class PlaceStatusRepository extends BaseRepository {
    private PlaceStatusApiService api;

    public PlaceStatusRepository() {
        this.api = RetrofitClient.getInstance().getPlaceStatusApi();
    }

    // 1. Tạo mới Status
    public void createPlaceStatus(PlaceStatusRequest request, DataCallback<PlaceStatus> callback) {
        makeCall(api.createPlaceStatus(request), callback);
    }

    // 2. Lấy Status theo Place ID (Thường dùng nhất)
    public void getStatusByPlaceId(String placeId, DataCallback<PlaceStatus> callback) {
        makeCall(api.getStatusByPlaceId(placeId), callback);
    }

    // 3. Cập nhật Status theo Place ID
    public void updateStatusByPlaceId(String placeId, PlaceStatusRequest request, DataCallback<PlaceStatus> callback) {
        makeCall(api.updateStatusByPlaceId(placeId, request), callback);
    }

    // 4. Xóa Status theo Place ID
    public void deleteStatusByPlaceId(String placeId, DataCallback<Void> callback) {
        makeCall(api.deleteStatusByPlaceId(placeId), callback);
    }

    // 5. Cập nhật Status theo ID riêng của Status (Ít dùng hơn, nhưng có trong API)
    public void updateStatusById(String statusId, PlaceStatusRequest request, DataCallback<PlaceStatus> callback) {
        makeCall(api.updateStatusById(statusId, request), callback);
    }
}
