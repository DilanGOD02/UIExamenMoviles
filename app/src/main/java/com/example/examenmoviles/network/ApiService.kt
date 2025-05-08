package com.example.examenmoviles.network
import com.example.examenmoviles.models.Course
import com.example.examenmoviles.models.Student
import retrofit2.http.GET

interface ApiService {
    @GET("api/student")
    suspend fun getStudents(): List<Student>



    //Course
    @GET("api/courses")
    suspend fun getCourses(): List<Course>
}
