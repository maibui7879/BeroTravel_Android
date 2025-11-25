package com.example.berotravel20.data.api;

import android.text.TextUtils;
import com.example.berotravel20.data.local.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:5000/";
    private static Retrofit retrofit;

    public static Retrofit getClient(final SessionManager sessionManager) {

        if (retrofit == null) {

            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    String url = original.url().encodedPath();
                    String method = original.method();

                    boolean isPublicGet = method.equals("GET") && (
                            url.startsWith("/places/images") ||
                                    url.startsWith("/places/search/nearby") ||
                                    url.startsWith("/places/count") ||
                                    url.matches("/places/[^/]+$") ||          // /places/{id}
                                    url.matches("/reviews/[^/]+$") ||        // /reviews/{placeId}
                                    url.matches("/reviews/[^/]+/rating$") || // /reviews/{placeId}/rating
                                    url.startsWith("/votes")                  // GET /votes?target_id=...
                    );

                    boolean noAuthNeeded = isPublicGet ||
                            url.startsWith("/users/login") ||
                            url.startsWith("/users/register");

                    Request.Builder requestBuilder = original.newBuilder();

                    if (!noAuthNeeded) {
                        String token = sessionManager.getToken();
                        if (!TextUtils.isEmpty(token)) {
                            requestBuilder.addHeader("Authorization", "Bearer " + token);
                        }
                    }

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
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
