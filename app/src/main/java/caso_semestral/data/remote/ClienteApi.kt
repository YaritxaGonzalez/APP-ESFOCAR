package com.example.caso_semestral.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteApi {

    // Emulador de Android Studio: 10.0.2.2 es el PC donde corre el backend.
    // Teléfono físico: cambia por la IP de tu PC en la red Wi-Fi, ej. "http://192.168.1.50:8081/api/"
    // (el teléfono y el PC deben estar en la misma red). Debe terminar en "/".
    const val BASE_URL = "http://10.0.2.2:8081/api/"

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: EsfocarApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EsfocarApi::class.java)
    }
}