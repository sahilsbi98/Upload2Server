package com.example.upload2server;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    public static Retrofit retrofit;
    private static final String SERVER = "http://10.0.2.2:3000/";

    public static Retrofit getRetrofit(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        if(retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(SERVER).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
        }
        return retrofit;
    }
}
