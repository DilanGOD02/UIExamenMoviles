package com.example.examenmoviles.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examenmoviles.models.Student
import com.example.examenmoviles.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentViewModel : ViewModel() {
    private val apiService = RetrofitClient.StudentapiService
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students

    // For UI feedback
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _loadingFromLocal = MutableStateFlow(false)
    val loadingFromLocal: StateFlow<Boolean> = _loadingFromLocal

    fun updateStudents(localStudents: List<Student>) {
        _students.value = localStudents
    }

    fun fetchAllStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAllStudents()
                Log.d("StudentViewModel", "Fetched ${response.size} students")
                _students.value = response
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students", e)
                _errorMessage.value = "Error fetching students: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchStudentsByCourseId(courseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getStudentsByCourseId(courseId)
                Log.d("StudentViewModel", "Fetched ${response.size} students for courseId $courseId")
                _students.value = response
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students by courseId", e)
                _errorMessage.value = "Error fetching students by courseId: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createStudent(student: Student) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.createStudent(student)
                _students.value += response
                _successMessage.value = "Student created successfully"
                Log.i("StudentViewModel", "Student created: $response")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Error creating student: ${e.message()}, Body: $errorBody"
                Log.e("StudentViewModel", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                _errorMessage.value = "Error creating student: ${e.message}"
                Log.e("StudentViewModel", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStudent(studentId: Int, student: Student) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.updateStudent(studentId, student)
                _students.value = _students.value.map {
                    if (it.id == studentId) response else it
                }
                _successMessage.value = "Student updated successfully"
                Log.i("StudentViewModel", "Student updated: $response")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Error updating student: ${e.message()}, Body: $errorBody"
                Log.e("StudentViewModel", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                _errorMessage.value = "Error updating student: ${e.message}"
                Log.e("StudentViewModel", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStudent(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                apiService.deleteStudent(studentId)
                _students.value = _students.value.filter { it.id != studentId }
                _successMessage.value = "Student deleted successfully"
                Log.i("StudentViewModel", "Student deleted")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _errorMessage.value = "Error deleting student: ${e.message()}, Body: $errorBody"
                Log.e("StudentViewModel", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting student: ${e.message}"
                Log.e("StudentViewModel", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }


}

