package com.etbc.eos.Retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @FormUrlEncoded
    @POST("registerFcmToken.php")
    Call<retrofitData> setFcmToken(@Field("userId") String userId, @Field("fcmToken") String fcmToken);
}
