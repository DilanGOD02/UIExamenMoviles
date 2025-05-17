package com.example.examenmoviles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examenmoviles.models.StudentDetails
import com.example.examenmoviles.network.RetrofitInstance
import com.example.examenmoviles.network.StudentApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentDetailsViewModel(app: Application) : AndroidViewModel(app){

    private val _students = MutableStateFlow<List<StudentDetails>>(emptyList())
    val students: StateFlow<List<StudentDetails>> = _students
    private val apiService = RetrofitInstance.studentApi

    fun fetchStudentsDetails() {
        viewModelScope.launch {
            try {
                _students.value = apiService.getAllStudents()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}