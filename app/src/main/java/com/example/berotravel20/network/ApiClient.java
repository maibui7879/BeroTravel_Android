package com.example.berotravel20.network;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:5001/api/"; // Android Emulator loopback to host
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        TokenManager tokenManager = new TokenManager(context.getApplicationContext());
                        String token = tokenManager.getToken();

                        if (token != null) {
                            android.util.Log.d("ApiClient", "Adding Authorization header: Bearer " + token);
                            builder.header("Authorization", "Bearer " + token);
                        } else {
                            android.util.Log.d("ApiClient", "No token found in TokenManager");
                        }

                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
