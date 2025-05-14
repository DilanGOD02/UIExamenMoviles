package com.example.examenmoviles.network
import com.example.examenmoviles.models.Student

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET

import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StudentApiService {

    @GET("api/students")
    suspend fun getAllStudents(): List<Student>

    @GET("api/students/{id}")
    suspend fun getStudentById(@Path("id") id: Int): Student

    @GET("api/students/course/{courseId}")
    suspend fun getStudentsByCourseId(@Path("courseId") courseId: Int): List<Student>

    @POST("api/students")
    suspend fun createStudent(@Body studentDto: Student): Student

    @PUT("api/students/{id}")
    suspend fun updateStudent(
        @Path("id") id: Int,
        @Body studentDto: Student
    ): Student

    @DELETE("api/students/{id}")
    suspend fun deleteStudent(@Path("id") id: Int)
}
