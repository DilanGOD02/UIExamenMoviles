package com.example.examenmoviles.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examenmoviles.models.Course
import com.example.examenmoviles.models.Student
import com.example.examenmoviles.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class StudentViewModel : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> get() = _students

    fun fetchStudents() {
        viewModelScope.launch {
            try {
                _students.value = RetrofitInstance.api.getStudents()
                Log.i("MyViewModel", "Fetching data from API... ${_students.value}")
            } catch (e: Exception) {
                Log.e("ViewmodelError", "Error: ${e}")
            }
        }
    }

}
