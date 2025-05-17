package com.example.examenmoviles.network


import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5275/"

    private const val CACHE_SIZE = 10 * 1024 * 1024 // 10 MiB

    // Crear la caché
    private fun createCache(context: Context): Cache {
        return Cache(context.cacheDir, CACHE_SIZE.toLong())
    }


    // Crear el cliente OkHttp con caché
    private fun createOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(createCache(context)) // Agregar la caché al cliente
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val CourseapiService: CourseApiService by lazy {
        retrofit.create(CourseApiService::class.java)
    }

    val StudentapiService: StudentApiService by lazy {
        retrofit.create(StudentApiService::class.java)
    }


    val courseApi: CourseApi by lazy {
        retrofit.create(CourseApi::class.java)
    }
}
