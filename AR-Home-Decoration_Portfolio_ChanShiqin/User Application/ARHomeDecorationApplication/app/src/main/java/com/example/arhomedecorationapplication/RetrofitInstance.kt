package com.example.arhomedecorationapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api : ApiInterface by lazy {
        Retrofit.Builder()
            // tx house wifi
//            .baseUrl("http://192.168.0.123/stripe-android-api/")
            // phone hostspot
//            .baseUrl("http://172.20.10.4/stripe-android-api/")
            // home wifi
            .baseUrl("http://192.168.0.249/stripe-android-api/")
            // school wifi
//            .baseUrl("http://172.17.3.200/stripe-android-api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

}