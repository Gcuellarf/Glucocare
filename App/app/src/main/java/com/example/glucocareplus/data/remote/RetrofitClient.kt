package com.example.glucocareplus.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Cambiar a la IP de tu PC si usas celular físico conectado por Wi-Fi
    private const val BASE_URL = "http://192.168.0.137/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}