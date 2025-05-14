package com.example.examenmoviles.network
import com.example.examenmoviles.models.Course
import com.example.examenmoviles.models.Student
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface CourseApiService {
    @GET("api/student")
    suspend fun getStudents(): List<Student>



    //Course
    @GET("api/courses")
    suspend fun getCourses(): List<Course>

    @GET("api/courses/{id}")
    suspend fun getCourseById(@Path("id") id: Int): Course

    @Multipart
    @POST("api/courses")
    suspend fun addCourse(
        @Part file: MultipartBody.Part,
        @PartMap courseData: Map<String, @JvmSuppressWildcards RequestBody>
    ): Course

    @Multipart
    @PUT("api/courses/{id}")
    suspend fun updateCourseWithImage(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part,
        @PartMap courseData: Map<String, @JvmSuppressWildcards RequestBody>
    ): Course

    @PUT("api/courses/{id}")
    suspend fun updateCourse(
        @Path("id") id: Int,
        @PartMap courseData: Map<String, @JvmSuppressWildcards RequestBody>
    ): Course

    @DELETE("api/courses/{id}")
    suspend fun deleteCourse(@Path("id") id: Int)
}
