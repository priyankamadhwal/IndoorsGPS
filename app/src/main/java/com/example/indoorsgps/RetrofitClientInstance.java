package com.example.indoorsgps;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {

    private static Retrofit retrofit;
    public static final String BASE_URL = "http://192.168.0.104:3000"; //"http://192.168.0.104:3000"; //"http://192.168.29.168:3000"; //"http://192.168.43.94:3000";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
