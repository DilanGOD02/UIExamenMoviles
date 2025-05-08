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
}
