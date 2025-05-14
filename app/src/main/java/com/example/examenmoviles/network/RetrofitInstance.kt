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
}