package com.example.berotravel20.data.api;

import com.example.berotravel20.data.model.Favorite.FavoriteResponse;
import com.example.berotravel20.data.model.Place.Place; // Import Model Place
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoriteApiService {

    // [SỬA ĐỔI] Trả về List<Place> thay vì List<String>
    @GET("api/favorites")
    Call<List<Place>> getMyFavorites();

    @POST("api/favorites/{placeId}")
    Call<FavoriteResponse> toggleFavorite(@Path("placeId") String placeId);
}