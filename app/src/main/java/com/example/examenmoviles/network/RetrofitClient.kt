package com.example.examenmoviles.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5275/"

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
