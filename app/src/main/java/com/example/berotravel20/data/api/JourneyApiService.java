package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Journey.Journey;
import retrofit2.Call;
import retrofit2.http.*;

public interface JourneyApiService {

    // 1. Lấy tất cả Journey
    @GET("api/journeys")
    Call<Journey.Response> getJourneys();

    // 2. Tạo Journey mới
    @POST("api/journeys")
    Call<Void> createJourney(@Body Journey.Request request);

    // 3. Lấy chi tiết Journey
    @GET("api/journeys/{id}")
    Call<Journey.DetailResponse> getJourneyById(@Path("id") String id);

    // 4. Cập nhật Journey (Thay đổi danh sách địa điểm)
    // Body: { "places": ["id_mới_1", "id_mới_2"] }
    @PUT("api/journeys/{id}")
    Call<Void> updateJourney(@Path("id") String id, @Body Journey.Request request);

    // 5. Xóa Journey
    @DELETE("api/journeys/{id}")
    Call<Void> deleteJourney(@Path("id") String id);

    // 6. Cập nhật trạng thái (ongoing/suspended)
    @PUT("api/journeys/{id}/status")
    Call<Void> updateStatus(@Path("id") String id, @Body Journey.StatusRequest request);

    // 7. Check-in địa điểm
    @POST("api/journeys/{journeyId}/places/{placeId}/visit")
    Call<Void> markAsVisited(@Path("journeyId") String journeyId, @Path("placeId") String placeId);
}