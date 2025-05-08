package com.example.examenmoviles.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examenmoviles.models.Course
import com.example.examenmoviles.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.viewModelScope
import com.example.examenmoviles.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import kotlin.collections.plus

class CourseViewModel : ViewModel() {
    private val apiService = RetrofitClient.apiService
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val course: StateFlow<List<Course>> = _courses

    fun fetchEvents() {
        viewModelScope.launch {
            try {
                val response = apiService.getCourses()
                Log.d("CourseViewModel", "Fetched ${response.size} courses") // <- Agregado
                _courses.value = response
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error fetching courses", e)
            }
        }
    }

    fun addCourse(course: Course, imageFile: File?) {
        viewModelScope.launch {
            try {
                if (imageFile == null) {
                    Log.e("ViewModelError", "Image file is required.")
                    return@launch
                }

                // 1. Convertir el archivo en MultipartBody.Part
                val requestFile = imageFile
                    .asRequestBody("image/*".toMediaTypeOrNull())

                val filePart = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = imageFile.name,
                    body = requestFile
                )

                // 2. Convertir los campos del evento en RequestBody
                val eventData: Map<String, RequestBody> = mutableMapOf<String, RequestBody>().apply {
                    put("name", course.name.toRequestBody("text/plain".toMediaType()))
                    put("description", course.description.toRequestBody("text/plain".toMediaType()))
                    put("schedule", course.schedule.toRequestBody("text/plain".toMediaType()))
                    put("professor", course.professor.toRequestBody("text/plain".toMediaType()))
                }

                Log.i("ViewModelInfo", "Sending event with image: ${course.name}")

                // 3. Llamar al backend
                val response = RetrofitInstance.api.addCourse(filePart, eventData)

                // 4. Agregarlo a la lista observable
                _courses.value += response

                //eventDao.insertAll(listOf(response)) // para guardar el nuevo evento en Room

                Log.i("ViewModelInfo", "Course created: $response")

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ViewModelError", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                Log.e("ViewModelError", "Error: ${e.message}", e)
            }
        }
    }
}
