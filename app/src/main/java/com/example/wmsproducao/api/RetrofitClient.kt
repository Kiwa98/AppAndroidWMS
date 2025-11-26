package com.example.wmsproducao.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://wmsapi-1.onrender.com"   // <-- MUDAR !!

    val instance: WmsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WmsApi::class.java)
    }
}
