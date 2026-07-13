package com.uniku.fkomeroom.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // URL Ngrok kamu
    private const val BASE_URL = "https://tapioca-expose-facing.ngrok-free.dev/api/"

    // 1. Buat OkHttpClient dengan Interceptor untuk melewati peringatan Ngrok
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("ngrok-skip-browser-warning", "true") // Kunci untuk bypass warning
                .build()
            chain.proceed(newRequest)
        }
        // 2. Tambah waktu timeout menjadi 30 detik agar lebih stabil
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // <-- PENTING: Gunakan okHttpClient di sini
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}