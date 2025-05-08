package com.example.examenmoviles.network
import com.example.examenmoviles.models.Course
import com.example.examenmoviles.models.Student
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ApiService {
    @GET("api/student")
    suspend fun getStudents(): List<Student>



    //Course
    @GET("api/courses")
    suspend fun getCourses(): List<Course>

    @Multipart
    @POST("api/courses")
    suspend fun addCourse(
        @Part file: MultipartBody.Part,
        @PartMap courseData: Map<String, @JvmSuppressWildcards RequestBody>
    ): Course
}
