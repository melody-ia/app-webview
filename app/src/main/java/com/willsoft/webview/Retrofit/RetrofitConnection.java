package com.willsoft.webview.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {

    String URL = "http://wallet.willsoft.kr/wallet/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public RetrofitInterface  server = retrofit.create(RetrofitInterface.class);
}