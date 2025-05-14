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
import com.example.examenmoviles.network.CourseApiService
import com.example.examenmoviles.network.RetrofitInstance
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import kotlin.collections.plus

class CourseViewModel : ViewModel() {
    private val apiService = RetrofitClient.CourseapiService
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val course: StateFlow<List<Course>> = _courses


    // For UI feedback
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

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

    fun getCourseById(courseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val course = apiService.getCourseById(courseId)
                Log.i("ViewModelInfo", "Fetched course: $course")
                // Aquí puedes hacer algo con el curso obtenido, como actualizar el estado o mostrarlo en la UI
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Error al obtener curso: ${e.message()}"
                Log.e("ViewModelError", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                _errorMessage.value = "Error al obtener curso: ${e.message}"
                Log.e("ViewModelError", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
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
                val response = RetrofitInstance.courseApi.addCourse(filePart, eventData)

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


    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.courseApi.deleteCourse(courseId)
                _courses.value = _courses.value.filter { it.id != courseId }
                Log.i("ViewModelInfo", "Course delete")
                Log.i("ViewModelInfo", "Course deleted successfully")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Error al eliminar curso: ${e.message()}"
                Log.e("ViewModelError", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar curso: ${e.message}"
                Log.e("ViewModelError", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
        }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    fun updateCourse(courseId: Int, course: Course, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val filePart = imageFile?.let { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        name = "file",
                        filename = file.name,
                        body = requestFile
                    )
                }

                val courseData = mapOf(
                    "name" to course.name.toRequestBody("text/plain".toMediaType()),
                    "description" to course.description.toRequestBody("text/plain".toMediaType()),
                    "professor" to course.professor.toRequestBody("text/plain".toMediaType()),
                    "schedule" to course.schedule.toRequestBody("text/plain".toMediaType())
                )

                val response = if (filePart != null) {
                    RetrofitInstance.courseApi.updateCourseWithImage(courseId, filePart, courseData)
                } else {
                    RetrofitInstance.courseApi.updateCourse(courseId, courseData)
                }

                _courses.value = _courses.value.map {
                    if (it.id == courseId) response else it
                }
                _successMessage.value = "Curso actualizado exitosamente"
                Log.i("ViewModelInfo", "Course updated: $response")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Error al actualizar curso: ${e.message()}"
                Log.e("ViewModelError", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar curso: ${e.message}"
                Log.e("ViewModelError", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }



    }
