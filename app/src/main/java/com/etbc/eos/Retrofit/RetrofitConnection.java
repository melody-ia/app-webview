package com.etbc.eos.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {

    String URL = "http://211.238.13.151/mou/wallet/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public RetrofitInterface  server = retrofit.create(RetrofitInterface.class);
}
