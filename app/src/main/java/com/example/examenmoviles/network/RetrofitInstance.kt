package com.example.examenmoviles.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: CourseApiService by lazy {
        Retrofit.Builder()
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5275/") // Change to your API URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CourseApiService::class.java)
    }

    private const val BASE_URL = "http://10.0.2.2:5275/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    val courseApi: CourseApi by lazy {
        retrofit.create(CourseApi::class.java)
    }
}