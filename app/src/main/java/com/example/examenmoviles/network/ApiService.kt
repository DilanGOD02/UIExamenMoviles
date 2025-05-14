package com.example.examenmoviles.network
import com.example.examenmoviles.models.Course
import com.example.examenmoviles.models.Student
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface ApiService {
    @GET("api/student")
    suspend fun getStudents(): List<Student>

}
