package com.example.examenmoviles.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examenmoviles.models.Student
import com.example.examenmoviles.network.RetrofitInstance
import com.example.examenmoviles.models.StudentAppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.text.insert

class StudentViewModel(app: Application) : AndroidViewModel(app) {

    private val db         = StudentAppDatabase.getInstance(app.applicationContext)
    private val apiService = RetrofitInstance.studentApi


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

    val isLoadingFromLocal = mutableStateOf(false)
    val showOfflineAlert   = mutableStateOf(false)

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

    private fun loadStudentsFromCache() {
        viewModelScope.launch {
            val local = withContext(Dispatchers.IO) {
                db.studentDao().getAll()
            }
            _students.value = local
            isLoadingFromLocal.value = true
            showOfflineAlert.value = true
            Log.d("StudentVM", "Cargando lista desde Room (items=${local.size})")
        }
    }


    fun fetchStudentsByCourseId(courseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Intenta obtener los datos desde la API
                val response = apiService.getStudentsByCourseId(courseId)
                Log.d("StudentViewModel", "Fetched ${response.size} students for courseId $courseId")
                _students.value = response

                // Actualiza el caché con los datos obtenidos de la API
                withContext(Dispatchers.IO) {
                    db.studentDao().insertAll(response)
                }
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students by courseId", e)
                _errorMessage.value = "Error fetching students by courseId: ${e.message}"

                // Si ocurre un error, carga los datos del caché local
                val cachedStudents = withContext(Dispatchers.IO) {
                    db.studentDao().getStudentsByCourseId(courseId)
                }
                _students.value = cachedStudents
                Log.d("StudentViewModel", "Loaded ${cachedStudents.size} students from cache for courseId $courseId")
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

    private suspend fun cacheStudent(student: Student) {
        db.studentDao().insert(student) // Cambia insertAll por insert
        _students.value = (_students.value.filter { it.id != student.id } + student)
            .distinctBy { it.id }
    }

    private fun hasNetwork(): Boolean {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}

